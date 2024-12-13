# app.py
from flask import Flask, render_template, request, jsonify
import pandas as pd
import numpy as np
import tensorflow as tf
from google.cloud import firestore
import firebase_admin
from firebase_admin import credentials, firestore
import joblib
from functools import lru_cache
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import TfidfVectorizer
import requests
from fuzzywuzzy import process

app = Flask(__name__)

cred = credentials.Certificate('config/capstone-project-441906-179ceddf7d34.json')
firebase_admin.initialize_app(cred)
db = firestore.client()
users_collection = db.collection('users')

API_KEY = 'AIzaSyA7YpgX2ISV3iHdJvGvaCEoW8WTbNYD0Cw'

def hash_password(password):
    """
    Hashes a password using SHA256.
    """
    return hashlib.sha256(password.encode()).hexdigest()

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
    """
    Membuat mapping antara nama resmi dan alias tempat wisata
    """
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
    """
    Mencari nama tempat terdekat dengan toleransi kesalahan menggunakan fuzzy matching
    
    Args:
        place_name (str): Nama tempat yang diinput
        tourism_df (pd.DataFrame): DataFrame berisi daftar tempat wisata
        threshold (int): Ambang batas kemiripan (default 80)
    
    Returns:
        str: Nama tempat terdekat yang paling cocok
    """
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

def get_place_image_url(place_name):
    """
    Fetches the image URL of a place using the Google Places API.
    """
    try:
        # Step 1: Find Place from Text
        search_url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json"
        search_params = {
            "input": place_name,
            "inputtype": "textquery",
            "fields": "place_id",
            "key": API_KEY
        }
        search_response = requests.get(search_url, params=search_params)
        search_response.raise_for_status()  # Raise HTTPError for bad responses
        search_data = search_response.json()

        if not search_data.get('candidates'):
            return None

        place_id = search_data['candidates'][0]['place_id']

        # Step 2: Get Place Details
        details_url = "https://maps.googleapis.com/maps/api/place/details/json"
        details_params = {
            "place_id": place_id,
            "fields": "photos",
            "key": API_KEY
        }
        details_response = requests.get(details_url, params=details_params)
        details_response.raise_for_status()
        details_data = details_response.json()

        photos = details_data.get('result', {}).get('photos', [])
        if not photos:
            return None

        photo_reference = photos[0]['photo_reference']

        # Step 3: Build Photo URL
        photo_url = f"https://maps.googleapis.com/maps/api/place/photo"
        return f"{photo_url}?maxwidth=400&photo_reference={photo_reference}&key={API_KEY}"
    
    except requests.exceptions.RequestException as e:
        print(f"Request error for {place_name}: {e}")
        return None
    except Exception as e:
        print(f"Unexpected error fetching image URL for {place_name}: {e}")
        return None

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.json
        place_name = data.get('place_name')
        model, tfidf_vectorizer, label_encoder, tourism_df = load_models()
        
        recommendations, category, category_probs, matched_place_name = get_recommendations_nn(
            place_name=place_name,
            tourism_df=tourism_df,
            model=model,
            tfidf_vectorizer=tfidf_vectorizer,
            label_encoder=label_encoder
        )

        # Add image URLs and assign unique IDs to each recommendation
        # for idx, rec in enumerate(recommendations, start=1): 
        #     rec['id'] = idx
        #     rec['image_url'] = get_place_image_url(rec['name'])

        if recommendations:
            first_image_url = get_place_image_url(recommendations[0]['name'])
        else:
            first_image_url = None  # Fallback if no recommendations are provided

        for idx, rec in enumerate(recommendations, start=1): 
            rec['id'] = idx
            rec['image_url'] = first_image_url

        return jsonify({
            'error': False,
            'code': 200,
            'data': {
                'category_probabilities': category_probs,
                'predicted_category': category,
                'recommendations': recommendations
            }
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        })

@app.route('/register', methods=['POST'])
def register():
    # Parse the incoming JSON request
    data = request.json
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')

    # Input validation
    if not username or not email or not password:
        return {
            "error": True,
            "message": "Username, email, and password are required."
        }, int(400)

    # References to the Firestore documents
    user_ref = db.collection('users').document(username)
    counter_ref = db.collection('metadata').document('user_counter')

    try:
        # Check if the user already exists
        if user_ref.get().exists:
            return {
                "error": True,
                "message": f"User {username} already exists."
            }, int(400)

        # Increment the userID counter
        counter_snapshot = counter_ref.get()
        if counter_snapshot.exists:
            current_id = counter_snapshot.to_dict().get('latest_id', 0)
            new_id = current_id + 1
        else:
            new_id = 1  # Start at 1 if the counter doesn't exist

        # Update the counter document
        counter_ref.set({'latest_id': new_id}, merge=True)

        # Create a new user document
        user_ref.set({
            "userID": new_id,
            "username": username,
            "email": email,
            "password": password,
            "created_at": firestore.SERVER_TIMESTAMP
        })

        return {
            "error": False,
            "message": f"User {username} registered successfully!",
            "userID": str(new_id),
            "email": email,
            "username": username
        }, int(201)

    except Exception as e:
        return {
            "error": True,
            "message": f"An error occurred: {str(e)}"
        }, int(500)

@app.route('/login', methods=['POST'])
def login():
    try:
        # Get the data from the request
        data = request.json
        email = data.get('email')
        password = data.get('password')

        # Validate the inputs
        if not email or not password:
            return jsonify({"error": True, "message": "Both email and password are required."}), int(400)

        # Check if the email exists
        user_query = users_collection.where('email', '==', email).get()
        if not user_query:
            return jsonify({"error": True, "message": "Invalid email or password."}), int(401)

        # Verify password
        user_data = user_query[0].to_dict()  # Assume email is unique and fetch the first match
        if user_data['password'] != password:
            return jsonify({"error": True, "message": "Invalid email or password."}), int(401)

        return jsonify({
            "error": False,
            "message": "Login successful.",
            "userID": str(user_data["userID"]),
            "username": user_data["username"],
            "email": email
        }), int(200)
    except Exception as e:
        return jsonify({"error": True, "message": str(e)}), int(500)

@app.route('/categories', methods=['POST'])
def update_categories():
    try:
        # Get the data from the request
        data = request.json
        userID = int(data.get('userID'))  # userID used to locate the user's document
        category_preferences = {
            "TamanHiburan": data.get('TamanHiburan', False),
            "Budaya": data.get('Budaya', False),
            "Bahari": data.get('Bahari', False),
            "CagarAlam": data.get('CagarAlam', False),
            "PusatPerbelanjaan": data.get('PusatPerbelanjaan', False),
            "TempatIbadah": data.get('TempatIbadah', False)
        }

        # Validate userID
        if not isinstance(userID, int):
            return jsonify({"error": True, "message": "userID must be an integer."}), int(400)

        # Validate category preferences
        if not all(isinstance(value, bool) for value in category_preferences.values()):
            return jsonify({"error": True, "message": "All category values must be Boolean."}), int(400)

        # Check if the userID exists in Firestore
        user_query = users_collection.where('userID', '==', userID).get()
        if not user_query:
            return jsonify({"error": True, "message": "User not found."}), int(404)

        # Assume the first document in the query result is the correct one
        user_ref = user_query[0].reference

        # Update the user's preferences in Firestore
        user_ref.update({"category_preferences": category_preferences})

        return jsonify({
            "error": False,
            "message": "Category preferences updated successfully.",
            "category_preferences": category_preferences
        }), int(200)

    except Exception as e:
        return jsonify({"error": True, "message": str(e)}), int(500)

@app.route('/home', methods=['POST'])
def home_recommendations():
    try:
        data = request.json
        userID = int(data.get('userID'))

        # Validate userID
        if not isinstance(userID, int):
            return jsonify({"error": True, "message": "userID must be an integer."}), int(400)

        # Check if the userID exists in Firestore
        user_query = users_collection.where('userID', '==', userID).get()
        if not user_query:
            return jsonify({"error": True, "message": "User not found."}), int(404)

        user_doc = user_query[0].to_dict()
        preferences = user_doc.get("category_preferences", {})

        category_map = {
            "TamanHiburan": "Taman Hiburan",
            "Budaya": "Budaya",
            "Bahari": "Bahari",
            "CagarAlam": "Cagar Alam",
            "PusatPerbelanjaan": "Pusat Perbelanjaan",
            "TempatIbadah": "Tempat Ibadah"
        }
        selected_categories = [
            category_map[key] for key, value in preferences.items() if value
        ]

        if not selected_categories:
            return jsonify({"error": False, "code": int(int(200)), "data": []})

        # Load the dataset
        tourism_df = pd.read_csv('data/tourism_with_id.csv')
        ratings_df = pd.read_csv('data/tourism_rating.csv')

        # Merge ratings with the tourism dataset
        tourism_df = pd.merge(tourism_df, ratings_df, on='Place_Id', how='left')
        tourism_df['Rating'] = tourism_df['Rating'].fillna('No rating')  # Handle missing ratings

        # Filter rows where the Category column matches selected categories
        filtered_data = tourism_df[tourism_df['Category'].isin(selected_categories)]

        # Group by category and randomly pick one place from each category
        grouped_recommendations = filtered_data.groupby('Category').apply(
            lambda x: x.sample(1) if len(x) > 0 else None
        ).reset_index(drop=True)

        # Collect recommendations for at least one of each selected category
        recommendations = []
        added_places = set()  # To track already added places

        for _, row in grouped_recommendations.iterrows():
            if row["Place_Name"] not in added_places:  # Avoid duplicates
                recommendation = {
                    "category": row["Category"],
                    "category_match": True,
                    "city": row["City"],
                    "description": row["Description"][:int(200)] + '...' if len(row["Description"]) > int(200) else row["Description"],
                    "explanation": f"termasuk dalam kategori yang sama ({row['Category']})",
                    "name": row["Place_Name"],
                    "price": f"Rp {row['Price']:,}" if pd.notnull(row["Price"]) else "Unknown",
                    # "image_url": get_place_image_url(row["Place_Name"]),
                    "rating": float(row["Rating"])
                }
                recommendations.append(recommendation)
                added_places.add(row["Place_Name"])

        # Add additional random places from filtered data until the total is 50
        remaining_places = filtered_data[
            ~filtered_data['Place_Name'].isin(added_places)
        ]
        if not remaining_places.empty:
            additional_recommendations = remaining_places.sample(
                min(50 - len(recommendations), len(remaining_places))
            )
        for _, row in additional_recommendations.iterrows():
            if row["Place_Name"] not in added_places:  # Avoid duplicates
                recommendation = {
                    "category": row["Category"],
                    "category_match": True,
                    "city": row["City"],
                    "description": row["Description"][:int(200)] + '...' if len(row["Description"]) > int(200) else row["Description"],
                    "explanation": f"termasuk dalam kategori yang sama ({row['Category']})",
                    "name": row["Place_Name"],
                    "price": f"Rp {row['Price']:,}" if pd.notnull(row["Price"]) else "Unknown",
                    # "image_url": get_place_image_url(row["Place_Name"]),
                    "rating": float(row["Rating"])
                }
            recommendations.append(recommendation)
            added_places.add(row["Place_Name"])

        if recommendations:
            first_image_url = get_place_image_url(recommendations[0]['name'])
        else:
            first_image_url = None  # Fallback if no recommendations are provided

        for idx, recommendation in enumerate(recommendations, start=1):
            recommendation["ID"] = int(idx)
            recommendation["image_url"] = first_image_url

        return jsonify({"error": False, "code": int(int(200)), "data": recommendations[:50]})

    except Exception as e:
        return jsonify({"error": True, "message": str(e)}), int(500)

@app.route('/filter', methods=['POST'])
def filter_recommendations():
    try:
        # Parse JSON request data
        data = request.json
        userID = int(data.get('userID'))
        city = data.get('city')
        price_min = float(data.get('price_min', 0))
        price_max = float(data.get('price_max', float('inf')))
        rating_min = float(data.get('rating_min', 0))
        rating_max = float(data.get('rating_max', 5))
        sorting=str(data.get('sorting'))

        # Validate price_min and price_max
        try:
            price_min = float(price_min)
            price_max = float(price_max)
        except ValueError:
            return jsonify({"error": True, "message": "Invalid price_min or price_max."}), 400
        
        try:
            rating_min = float(rating_min)
            rating_max = float(rating_max)
        except ValueError:
            return jsonify({"error": True, "message": "Invalid rating_min or rating_max."}), 400

        # Validate userID
        if not isinstance(userID, int):
            return jsonify({"error": True, "message": "userID must be an integer."}), int(400)

        # Check if the userID exists in Firestore
        user_query = users_collection.where('userID', '==', userID).get()
        if not user_query:
            return jsonify({"error": True, "message": "User not found."}), int(404)

        user_doc = user_query[0].to_dict()
        preferences = user_doc.get("category_preferences", {})

        category_map = {
            "TamanHiburan": "Taman Hiburan",
            "Budaya": "Budaya",
            "Bahari": "Bahari",
            "CagarAlam": "Cagar Alam",
            "PusatPerbelanjaan": "Pusat Perbelanjaan",
            "TempatIbadah": "Tempat Ibadah"
        }
        selected_categories = [
            category_map[key] for key, value in preferences.items() if value
        ]

        if not selected_categories:
            return jsonify({"error": False, "code": int(int(200)), "data": []})

        # Load the dataset
        tourism_df = pd.read_csv('data/tourism_with_id.csv')
        ratings_df = pd.read_csv('data/tourism_rating.csv')

        if city:
            tourism_df = tourism_df[tourism_df['City'].str.lower() == city.lower()]

        tourism_df = tourism_df[
            (tourism_df['Price'] >= price_min) & (tourism_df['Price'] <= price_max)
        ]

        # Merge ratings with the tourism dataset
        tourism_df = pd.merge(tourism_df, ratings_df, on='Place_Id', how='left')
        tourism_df['Rating'] = tourism_df['Rating'].fillna('No rating')  # Handle missing ratings

        tourism_df = tourism_df[
            (tourism_df['Rating'] >= rating_min) & (tourism_df['Rating'] <= rating_max)
        ]

        # Filter rows where the Category column matches selected categories
        filtered_data = tourism_df[tourism_df['Category'].isin(selected_categories)]

        # Group by category and randomly pick one place from each category
        grouped_recommendations = filtered_data.groupby('Category').apply(
            lambda x: x.sample(1) if len(x) > 0 else None
        ).reset_index(drop=True)

        # Collect recommendations for at least one of each selected category
        recommendations = []
        added_places = set()  # To track already added places

        for _, row in grouped_recommendations.iterrows():
            if row["Place_Name"] not in added_places:  # Avoid duplicates
                recommendation = {
                    "category": row["Category"],
                    "category_match": True,
                    "city": row["City"],
                    "description": row["Description"][:int(200)] + '...' if len(row["Description"]) > int(200) else row["Description"],
                    "explanation": f"termasuk dalam kategori yang sama ({row['Category']})",
                    "name": row["Place_Name"],
                    "price": f"Rp {row['Price']:,}" if pd.notnull(row["Price"]) else "Unknown",
                    # "image_url": get_place_image_url(row["Place_Name"]),
                    "rating": float(row["Rating"])
                }
                recommendations.append(recommendation)
                added_places.add(row["Place_Name"])

        # Add additional random places from filtered data until the total is 50
        remaining_places = filtered_data[
            ~filtered_data['Place_Name'].isin(added_places)
        ]
        if not remaining_places.empty:
            additional_recommendations = remaining_places.sample(
                min(50 - len(recommendations), len(remaining_places))
            )
        for _, row in additional_recommendations.iterrows():
            if row["Place_Name"] not in added_places:  # Avoid duplicates
                recommendation = {
                    "category": row["Category"],
                    "category_match": True,
                    "city": row["City"],
                    "description": row["Description"][:int(200)] + '...' if len(row["Description"]) > int(200) else row["Description"],
                    "explanation": f"termasuk dalam kategori yang sama ({row['Category']})",
                    "name": row["Place_Name"],
                    "price": f"Rp {row['Price']:,}" if pd.notnull(row["Price"]) else "Unknown",
                    # "image_url": get_place_image_url(row["Place_Name"]),
                    "rating": float(row["Rating"])
                }
            recommendations.append(recommendation)
            added_places.add(row["Place_Name"])

        unique_items = {}
        filtered_data = []

        for item in recommendations:
            unique_key = item['name']  # You can change this to 'name' or a combination of fields if needed
            if unique_key not in unique_items:
                unique_items[unique_key] = True
                filtered_data.append(item)

        if sorting == "price_desc":
            filtered_data.sort(key=lambda x: x["price"], reverse=True)
        elif sorting == "price_asc":
            filtered_data.sort(key=lambda x: x["price"])
        elif sorting == "rating_desc":
            filtered_data.sort(key=lambda x: x["rating"], reverse=True)
        elif sorting == "rating_asc":
            filtered_data.sort(key=lambda x: x["rating"])

        if filtered_data:
            first_image_url = get_place_image_url(filtered_data[0]['name'])
        else:
            first_image_url = None  # Fallback if no filtered_data are provided

        for idx, recommendation in enumerate(filtered_data, start=1):
            recommendation["ID"] = int(idx)
            recommendation["image_url"] = first_image_url

        return jsonify({"error": False, "code": int(int(200)), "data": filtered_data[:50]})

    except Exception as e:
        return jsonify({"error": True, "message": str(e)}), int(500)

if __name__ == '__main__':
    app.run(debug=True)
