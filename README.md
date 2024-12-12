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

```JSON
  {
    "email": "johndoexample@com"
    "password": "securepassword"
    "username": "Johndoe"
  }
```
**Response**
```JSON
{
  "message": "Email successfully registered"
  "status": "success"
}
```



