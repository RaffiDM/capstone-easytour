from flask import Flask, render_template, request, jsonify
import pandas as pd
import numpy as np
import tensorflow as tf
import joblib
from functools import lru_cache
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
from fuzzywuzzy import process

app = Flask(__name__)

@lru_cache(maxsize=None)
def load_models():
    model = tf.keras.models.load_model('model/tourism_classifier.h5')
    tfidf_vectorizer = joblib.load('model/tfidf_vectorizer.pkl')
    label_encoder = joblib.load('model/label_encoder.pkl')
    tourism_df = pd.read_csv('data/tourism_with_id.csv')
    return model, tfidf_vectorizer, label_encoder, tourism_df

def prepare_place_features(tourism_df):
    tourism_df['text_features'] = tourism_df.apply(
        lambda x: f"{x['Place_Name']} {x['Description']} {x['City']}", axis=1
    )
    return tourism_df

def load_place_name_aliases():
    aliases = {
        'monas': 'Monumen Nasional',
        'dufan': 'Dunia Fantasi',
        'tmii': 'Taman Mini Indonesia Indah (TMII)',
        'taman mini': 'Taman Mini Indonesia Indah (TMII)',
        'ancol': 'Taman Impian Jaya Ancol',
        'ragunan': 'Kebun Binatang Ragunan',
        'marina': 'Pantai Marina',
        'seaworld': 'Sea World',
        'grand indonesia': 'Grand Indonesia Mall',
        'taman pintar': 'Taman Pintar Yogyakarta',
        'taman sari' : 'Kampung Wisata Taman Sari',
        '0 kilometer' : 'Nol Kilometer Jl.Malioboro',
        'nol kilometer': 'Nol Kilometer Jl.Malioboro',
        'gembira loka' : 'Gembira Loka Zoo',
        'monjali' : 'Monumen Yogya Kembali',
        'donotirto': 'Candi Donotirto',
        'malioboro': 'Kawasan Malioboro',
        'prambanan': 'Candi Prambanan',
        'borobudur': 'Candi Borobudur',
        'parangtritis': 'Pantai Parangtritis',
        'trans studio': 'Trans Studio Bandung',
        'maerakaca': 'Grand Maerakaca',
        'sam poo kong': 'Sam Poo Kong Temple',
        'saloka': 'Saloka Theme Park',
        'celosia': 'Taman Bunga Celosia',
        'rawa pening': 'Danau Rawa Pening',
        'water blaster': 'Water Blaster Bukit Candi Golf',
        'eling bening': 'Wisata Eling Bening',
        'tugu muda': 'Tugu Muda Semarang',
        'banaran': 'Kampoenng Kopi Banaran',
        'goa kreo': 'Obyek Wisata Goa Kreo'
    }
    return aliases

def fuzzy_match_place_name(place_name, tourism_df, threshold=80):
    # Load aliases
    aliases = load_place_name_aliases()
    
    # Normalisasi input
    normalized_place_name = place_name.lower().strip()
    
    # Pertama, cek apakah ada di aliases
    if normalized_place_name in aliases:
        return aliases[normalized_place_name]
    
    # Ambil semua nama tempat unik dari dataset
    place_names = tourism_df['Place_Name'].unique().tolist()
    
    # Tambahkan aliases ke daftar nama tempat
    place_names.extend(list(aliases.values()))
    place_names = list(set(place_names))  # Hapus duplikat
    
    # Gunakan fuzzywuzzy untuk mencari kecocokan terdekat
    best_match = process.extractOne(normalized_place_name, place_names)
    
    # Kembalikan nama tempat terdekat jika di atas threshold
    if best_match[1] >= threshold:
        return best_match[0]
    
    return place_name  # Kembalikan nama asli jika tidak ada yang cocok

def predict_category(model, tfidf_vectorizer, label_encoder, text):
    input_vector = tfidf_vectorizer.transform([text]).toarray()
    prediction = model.predict(input_vector)
    
    probabilities = prediction[0]
    predicted_category_index = np.argmax(probabilities)
    predicted_category = label_encoder.inverse_transform([predicted_category_index])[0]
    
    categories = label_encoder.classes_
    category_probs = {cat: float(prob) for cat, prob in zip(categories, probabilities)}
    
    return predicted_category, category_probs, probabilities

def calculate_neural_network_similarity(input_probs, all_places_probs):
    return cosine_similarity(input_probs.reshape(1, -1), all_places_probs)[0]

def get_recommendations_nn(place_name, tourism_df, model, tfidf_vectorizer, label_encoder, n_recommendations=15):
    # Tambahkan fuzzy matching sebelum proses rekomendasi
    matched_place_name = fuzzy_match_place_name(place_name, tourism_df)
    
    # Prepare place features
    df = prepare_place_features(tourism_df.copy())
    
    # Case-insensitive search for the input place
    input_place = df[df['Place_Name'].str.lower() == matched_place_name.lower()]
    
    if input_place.empty:
        input_text = matched_place_name
    else:
        input_text = input_place['text_features'].iloc[0]
    
    # Predict category for input place
    predicted_category, category_probs, input_probs = predict_category(
        model=model,
        tfidf_vectorizer=tfidf_vectorizer,
        label_encoder=label_encoder,
        text=input_text
    )
    
    all_vectors = tfidf_vectorizer.transform(df['text_features']).toarray()
    all_predictions = model.predict(all_vectors)
    
    # Calculate neural network similarities
    nn_similarities = calculate_neural_network_similarity(input_probs, all_predictions)
    df['nn_similarity'] = nn_similarities
    
    # Exclude the original place from recommendations based on matched place name
    df_recommendations = df[df['Place_Name'].str.lower() != matched_place_name.lower()]
    
    # Sort recommendations based on neural network similarity
    recommendations = df_recommendations.nlargest(n_recommendations, 'nn_similarity')
    
    # If the input place was in the original dataframe, we'll add it back with its true similarity score
    if not input_place.empty:
        input_place_similarity = df.loc[input_place.index, 'nn_similarity'].values[0]
        similar_input_place = input_place.copy()
        similar_input_place['nn_similarity'] = input_place_similarity
        recommendations = pd.concat([similar_input_place, recommendations]).head(n_recommendations)
    
    detailed_recommendations = []
    
    for _, row in recommendations.iterrows():
        similarity_details = {
            'name': row['Place_Name'],
            'city': row['City'],
            'category': row['Category'],
            'price': f"Rp {row['Price']:,}",
            "rating": float(row["Rating"]),
            'description': row['Description'][:200] + '...' if len(row['Description']) > 200 else row['Description'],
            'nn_similarity_score': float(row['nn_similarity']),
            'explanation': f"Tempat ini direkomendasikan karena memiliki skor kemiripan neural network sebesar {row['nn_similarity']:.2f}."
        }
        detailed_recommendations.append(similarity_details)
    
    return detailed_recommendations, predicted_category, category_probs, matched_place_name

@app.route('/predict', methods=['POST'])
def predict():
    try:
        if not request.is_json:
            return jsonify({
                'success': False,
                'error': 'Request content type must be application/json.'
            }), 415
        
        data = request.get_json()
        if 'place_name' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing "place_name" in the request body.'
            }), 400
        
        place_name = data['place_name']
        model, tfidf_vectorizer, label_encoder, tourism_df = load_models()
        
        recommendations, category, category_probs, matched_place_name = get_recommendations_nn(
            place_name=place_name,
            tourism_df=tourism_df,
            model=model,
            tfidf_vectorizer=tfidf_vectorizer,
            label_encoder=label_encoder
        )
        
        return jsonify({
            'success': True,
            'predicted_category': category,
            'category_probabilities': category_probs,
            'recommendations': recommendations
        })
    
    except Exception as e:
        app.logger.error(f"Error processing request: {e}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


if __name__ == '__main__':
    app.run(debug=True)