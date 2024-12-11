# Easytour Model
[Dataset Link](https://www.kaggle.com/datasets/aprabowo/indonesia-tourism-destination)

## Project Overview

This is a machine learning-based tourism recommendation system that uses content-based filtering to suggest tourist destinations in Indonesia. The system leverages TensorFlow to create a neural network classifier that recommends similar tourist attractions based on textual features.

## Features

- Content-based recommendation using neural network similarity
- TF-IDF vectorization for text feature extraction
- Multi-class classification of tourism destinations
- Flask-based web API for recommendations
- Supports place name aliases for better matching

## Project Structure

```
tourism-recommendation/
│
├── data/
│   └── tourism_with_id.csv
│
├── model/
│   ├── label_encoder.pkl
│   ├── tfidf_vectorizer.pkl
│   ├── tourism_classifier.h5 
│   └── tourism_classifier.tflite 
│
├── notebook/
│   └── tourism_recommendation_system.ipynb 
├── app.py                         
├── requirements.txt         
└── README.md     
```

## Prerequisites

- Python 3.8+
- pip (Python package manager)

## Installation

1. Clone the repository:
   ```bash
   git clone --branch Machine-Learning https://github.com/RaffiDM/capstone-easytour.git
   cd capstone-easytour
   ```

2. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows, use `venv\Scripts\activate`
   ```

3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Running the Application

1. Start the Flask server:
   ```bash
   python app.py
   ```

2. Open a web browser and navigate to `http://localhost:5000`

## API Usage

### Endpoint: `/predict`

- **Method**: POST
- **Content Type**: application/json
- **Request Body**:
  ```json
  {
    "place_name": "Monumen Nasional"
  }
  ```

- **Response**:
  ```json
  {
    "success": true,
    "predicted_category": "Budaya",
    "category_probabilities": {...},
    "recommendations": [...]
  }
  ```

## Model Details

- **Model Type**: Neural Network Classifier
- **Feature Extraction**: TF-IDF Vectorization
- **Input Features**: Place Name, City, Description
- **Augmentation**: Text data augmentation for improved learning

## Performance Metrics

- Implemented early stopping
- L2 regularization
- Dropout layers 
