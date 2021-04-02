# Song Microservice
A Spring Boot project (which I created to learn Spring Cloud).


# Deployment
- clone repository
- create PostgreSQL db with 3 DBs (User, Concerts and Playlist)
- use user.sql, concerts.sql and playlist.sql to create tables in the corresponding DB (files are under the resources folder)
- setup env variables: 
  - **SONGS_PASSWORD**=db_password
  - **SONGS_HOST**=db_ip 
  - **KBE_DB_USER**=db_user
  - **SECRET_KEY_KBE**=key_used_to_create_JWT
  
- run all mains
  - Authorization Microservice
  - Song Microservice
  - Concert Microservice
  - Eureka Microservice
  - Gateway Microservice

---

# Endpoints

Endpoints documentation of Song web application.

## Indices

* [AUTH](#auth)

  * [Auth User](#1-auth-user)
  * [Auth User 2](#2-auth-user-2)

* [CONCERT](#concert)

  * [Delete a ticket](#1-delete-a-ticket)
  * [Get Concert by ID](#2-get-concert-by-id)
  * [Get Ticket](#3-get-ticket)
  * [Get Ticket QR](#4-get-ticket-qr)
  * [Get all Concerts](#5-get-all-concerts)
  * [Get all user tickets](#6-get-all-user-tickets)
  * [Post a new concert](#7-post-a-new-concert)

* [PLAYLIST](#playlist)

  * [Delete a specific Playlist](#1-delete-a-specific-playlist)
  * [Get all user playlists](#2-get-all-user-playlists)
  * [Get specific user playlists](#3-get-specific-user-playlists)
  * [Post a playlist](#4-post-a-playlist)
  * [Update a Playlist](#5-update-a-playlist)

* [SONG](#song)

  * [Delete Song by ID](#1-delete-song-by-id)
  * [Get Song by ID](#2-get-song-by-id)
  * [Get Songs from Artist](#3-get-songs-from-artist)
  * [Get all Songs](#4-get-all-songs)
  * [Post a Song](#5-post-a-song)
  * [Post a Song JSON File](#6-post-a-song-json-file)
  * [Put a Song](#7-put-a-song)


--------


## AUTH
Authorisation Endpoints.



### 1. Auth User


Get JWT for User 1.


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: http://localhost:8080/songsWS/rest/auth
```



***Body:***

```js        
{"userId":"mmuster","password":"pass1234"}
```



### 2. Auth User 2


Get JWT for User 2.


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: http://localhost:8080/songsWS/rest/auth
```



***Body:***

```js        
{"userId":"eschuler","password":"pass1234"}
```



## CONCERT
Concert endpoints.



### 1. Delete a ticket


Delete ticket by ID.


***Endpoint:***

```bash
Method: DELETE
Type: 
URL: http://localhost:8080/songsWS/rest/tickets/1
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



### 2. Get Concert by ID


Get concert by ID.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/concerts/1
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 3. Get Ticket


Get Ticket by ID.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/tickets
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| buyConcert | 1 |  |



### 4. Get Ticket QR


Get Ticket QR Code.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/tickets/1
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



### 5. Get all Concerts


Get all concerts.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/concerts/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 6. Get all user tickets


Get all tickets for user.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/tickets/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 7. Post a new concert


Post a new Concert.


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: http://localhost:8080/songsWS/rest/concerts/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



***Body:***

```js        
{
    "location": "Berlin",
    "artist": "Britney Spears",
    "maxTickets": 10
}
```



## PLAYLIST
Playlists endpoints.



### 1. Delete a specific Playlist


Delete a playlist by ID.


***Endpoint:***

```bash
Method: DELETE
Type: 
URL: http://localhost:8080/songsWS/rest/songLists/2
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



### 2. Get all user playlists


Get all the playlists from a User, if you own the playlist private and public will be returned else only public.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/songLists
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| userId | mmuster |  |



### 3. Get specific user playlists


Get a specific playlist by ID.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/songLists/2
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 4. Post a playlist


Post a new Playlist.


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: http://localhost:8080/songsWS/rest/songLists
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Content-Type | application/json |  |



***Body:***

```js        
{
    "name": "Mmuster's Private Playlist 2",
    "isPrivate": true,
    "songList": [
        {
            "id": 4,
            "title": "Ghostbusters (I'm not a fraid)",
            "artist": "Fall Out Boy, Missy Elliott",
            "label": "Ghostbusters",
            "released": 2016
        },
        {
            "id": 5,
            "title": "Bad Things",
            "artist": "Camila Cabello, Machine Gun Kelly",
            "label": "Bloom",
            "released": 2017
        }
    ]
}
```



### 5. Update a Playlist


Update a playlist.


***Endpoint:***

```bash
Method: PUT
Type: RAW
URL: http://localhost:8080/songsWS/rest/songLists/5
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Content-Type | application/json |  |



***Body:***

```js        
{
    "id": 5,
    "name": "Mmuster's Public Playlist 2 NEW",
    "isPrivate": false,
    "songList": [
        {
            "id": 8,
            "title": "No",
            "artist": "Meghan Trainor",
            "label": "Thank You",
            "released": 2016
        },
        {
            "id": 9,
            "title": "Private Show",
            "artist": "Britney Spears",
            "label": "Glory",
            "released": 2016
        }
    ]
}
```



## SONG
Song service endpoints.



### 1. Delete Song by ID


Delete a song from ID.


***Endpoint:***

```bash
Method: DELETE
Type: 
URL: http://localhost:8080/songsWS/rest/songs/5
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



### 2. Get Song by ID


Get a Song by the ID.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/songs/1
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 3. Get Songs from Artist


Get all songs from given artist.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/songs
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| artist | Justin Timberlake |  |



### 4. Get all Songs


Get all available songs.


***Endpoint:***

```bash
Method: GET
Type: 
URL: http://localhost:8080/songsWS/rest/songs/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Accept | application/json |  |



### 5. Post a Song


Post a new Song.


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: http://localhost:8080/songsWS/rest/songs/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



***Body:***

```js        
{
    "title": "Can’t Stop the Feeling",
    "artist": "Justin Timberlake", 
    "label": "Trolls",
    "released": 2016
}
```



### 6. Post a Song JSON File


Post a new Song as JSON File.


***Endpoint:***

```bash
Method: POST
Type: FILE
URL: http://localhost:8080/songsWS/rest/songs/
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |



### 7. Put a Song


Update a Song.


***Endpoint:***

```bash
Method: PUT
Type: RAW
URL: http://localhost:8080/songsWS/rest/songs/3
```


***Headers:***

| Key | Value | Description |
| --- | ------|-------------|
| Authorization | JWT |  |
| Content-Type | application/json |  |



***Body:***

```js        
{
    "id": 3,
    "title": "SONG_TITLE_EDIT",
    "artist": "COOL Artitst_EDIT", 
    "label": "SONY_EDIT",
    "released": 2021
}
```



---
[Back to top](#song-microservice)
