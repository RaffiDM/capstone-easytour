# Cloud Computing
As a **Cloud Computing** students, we are assigned to deploying our models to [Google Cloud Platform](https://cloud.google.com/?hl=en) by using [cloud run](https://cloud.google.com/run?hl=en), creating **RESTful APIs** for our app to be able to communicate and get the data from the model, as well as making the **Login** and **Register API**, and enable database to store our data using [Firestore](https://cloud.google.com/firestore?hl=en)

# RESTful API
In creating the **RESTful API** we are using [Python](https://github.com/python) programming language while also using the [Flask Framework](https://flask.palletsprojects.com/en/stable/) tools to make a stable **API**. Our **API** returns data in **JSON** format for each **URL**

# Places API
We are currently taking advantage of the [Google Maps platform](https://developers.google.com/maps) by using [Places API](https://developers.google.com/maps/documentation/places/web-service/overview) to provide our application with the places details and the places picture of the destination that the users are looking for

# RESTful API link destination
Below we will explain each endpoint of the URL about what the **API** does, the content, and the response we get from the **API**

**Base URL:**
> https://app-service-13797535012.asia-southeast2.run.app

**endpoint:**
> /register

**Method:**
> POST

**Example URL**
> https://app-service-13797535012.asia-southeast2.run.app/register

this endpoint is used to register the user email and password into the datababase with 3 value :
> email <br>
> password <br>
> username

```JSON
  {
    "email": "johndoe@example.com",
    "password": "securepassword",
    "username": "Johndoe"
  }
```
**Response**
```JSON
  {
    "email": "johndoe@example.com",
    "error": false,
    "message": "User Johndoe registered successfully!",
    "userID": "61",
    "username": "Johndoe"
  }
```


**endpoint:**
> /login

**Method:**
> POST

**Example URL**
> https://app-service-13797535012.asia-southeast2.run.app/login

this endpoint's function is to authenticate the user, checking if their email has been set in the database yet or not using 2 parameter:
> email <br>
> password <br>

```JSON
  {
    "email": "johndoe@example.com",
    "password": "securepassword"
  }
```
**Response**
```JSON
  {
    "email": "johndoe@example.com",
    "error": false,
    "message": "Login successful.",
    "userID": "61",
    "username": "Johndoe"
  }
```


**endpoint:**
> /categories

**Method:**
> POST

**Example URL**
> https://app-service-13797535012.asia-southeast2.run.app/categories

this endpoint is use to sent the category that the user wanted to search with the value of:
> taman hiburan <br>
> budaya <br>
> bahari <br>
> cagar alam <br>
> pusat perbelanjaan <br>
> tempat ibadah <br>

```JSON
  {
    "userID": "61",
    "TamanHiburan": true,
    "Budaya": false,
    "Bahari": true,
    "CagarAlam": false,
    "PusatPerbelanjaan": true, 
    "TempatIbadah": false
  }
```
**Response**
```JSON
  {
    "category_preferences": {
        "Bahari": true,
        "Budaya": false,
        "CagarAlam": false,
        "PusatPerbelanjaan": true,
        "TamanHiburan": true,
        "TempatIbadah": false
    },
    "error": false,
    "message": "Category preferences updated successfully."
  }
```


**endpoint:**
> /home

**Method:**
> POST

**Example URL**
> https://app-service-13797535012.asia-southeast2.run.app/home

this endpoint is used to direct the user to the homepage and send the recommendation based on the category that has been checklist by the user using the userId

```JSON
  {
    "userID": "61"
  }
```
**Response**
```JSON
  {
    "code": 200,
    "data": [
        {
            "ID": 1,
            "category": "Bahari",
            "category_match": true,
            "city": "Yogyakarta",
            "description": "Pantai Sanglen. Lokasinya berada di Desa Kemadang, Gunung Kidul. Pantai indah yang satu ini memang terdengar asing di telinga, namun ternyata pantai ini menyimpan keindahan yang sangat menenangkan. Ke...",
            "explanation": "termasuk dalam kategori yang sama (Bahari)",
            "image_url": "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=AWYs27wgh_qhce-HAhIre4sQggK6wxmG2crajngpzVMTdeyo7RygpnN7PG5TKG6aU9LF_uH4t6AMr96a-YfrkL1TjDUvQA__vhT168tDinePwnLC1DuG6PiOMCHgzKR-_I8f2JhhHYQy2pj9Rh-0h5iXjKQ24YcVKgeRtCDMbd-jlIDv6ZGn&key=AIzaSyA7YpgX2ISV3iHdJvGvaCEoW8WTbNYD0Cw",
            "name": "Pantai Sanglen",
            "price": "Rp 10,000",
            "rating": 4.5
        },
    ],
    "error": false
  }
```


**endpoint:**
> /filter

**Method:**
> POST

**Example URL**
> https://app-service-13797535012.asia-southeast2.run.app/filter

this endpoint is used to send the value that the user wants using the parameter stated below:
> city <br>
> price min <br>
> price max <br>
> rating min <br>
> rating max <br>
> sorting

```JSON
  {
    "userID" : "61",
    "city" : "Surabaya",
    "price_min" : 5000,
    "price_max" : 100000,
    "rating_min" : 4,
    "rating_max" : 4.4,
    "sorting" : "rating_desc"
  }
```
**Response**
```JSON
  {
    "code": 200,
    "data": [
        {
            "ID": 1,
            "category": "Taman Hiburan",
            "category_match": true,
            "city": "Surabaya",
            "description": "Surabaya menjadi kota besar yang sering menjadi tempat tujuan wisata. Berbagai tempat belanja dari tradisional hingga modern ada di Ibukota Jawa Timur ini. Namun, apakah tidak bosan wisata belanja saj...",
            "explanation": "termasuk dalam kategori yang sama (Taman Hiburan)",
            "image_url": "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photo_reference=AWYs27wRGPXi8lxo8MtHtdFEZ2u7Q6g4OWlJcUspvVUvVYxxBtqx3VM33ihxbMuBp-oU3oNoRfL4F_fj4ZjiCNk8Bzmsf2KjyYl4CdAAL0Pwo_mBPyJ-X6rYT1EPZRWSANi5KbLPEgbp3C-6YmcUKgYnCh7u6Y6734HYA-1AT8O5CLkwxfKz&key=AIzaSyA7YpgX2ISV3iHdJvGvaCEoW8WTbNYD0Cw",
            "name": "Surabaya North Quay",
            "price": "Rp 50,000",
            "rating": 4.4
        },
    ],
    "error": false
  }
```

