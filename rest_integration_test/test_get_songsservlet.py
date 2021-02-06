import concurrent
import json
import os
import unittest

import requests


class TestSongsservlet(unittest.TestCase):

    def setUp(self):
        if os.getenv("TEST_LOCALLY") == "True":
            self.URL = "http://localhost:8080"
        else:
            self.URL = "http://82.165.236.91:8080"
        self.get_song_URL = self.URL + "/songsservlet-MarvEn/songs?songId={song_id}"
        self.post_song_URL = self.URL + "/songsservlet-MarvEn/songs"

    def test_get_song_by_id_verify_status_code(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))
        self.assertEqual(requests.codes.ok, r.status_code)

    def test_get_song_by_id_verify_response_content_type(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))
        self.assertEqual('application/json;charset=UTF-8', r.headers['Content-Type'])

    def test_get_song_by_id_verify_content(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']

        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))

        self.assertEqual(
            {"artist": "MILEYCYRUS", "id": song_id, "label": "RCA", "title": "Wrecking Ball", "released": 2013},
            json.loads(r.text))

    def test_get_song_by_id_verify_keys_in_response(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))
        song_as_json_keys = list(json.loads(r.text).keys())
        self.assertTrue('title' in song_as_json_keys)
        self.assertTrue('id' in song_as_json_keys)
        self.assertTrue('label' in song_as_json_keys)
        self.assertTrue('released' in song_as_json_keys)
        self.assertTrue('artist' in song_as_json_keys)

    def test_get_song_by_id_verify_numeric_key_are_ints(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))
        song_as_json = json.loads(r.text)
        self.assertTrue(type(song_as_json['id']) is int)
        self.assertTrue(type(song_as_json['released']) is int)

    def test_get_song_by_id_edge_non_escaped_values(self):
        r = requests.post(self.post_song_URL,
                          data="""{"title": "test", "artist": "MILEYCYRUS", "label": "R'CA", 
                              "released": 2013}""", headers={'Content-Type': 'application/json'})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))

        self.assertEqual(
            {"artist": "MILEYCYRUS", "id": song_id, "label": "R'CA", "title": "test", "released": 2013},
            json.loads(r.text))

    def test_get_song_no_id_added(self):
        r = requests.get(self.URL + '/songsservlet-MarvEn/songs?songId=')
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_song_non_numeric_value_as_id(self):
        r = requests.get(self.get_song_URL.format(song_id='z'))
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_get_wrong_url(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])

        test_url = self.get_song_URL.format(song_id=song_id)
        test_url = test_url.replace("songId=", "songI=")
        r = requests.get(test_url)
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_song_not_in_database(self):
        r = requests.get(self.get_song_URL.format(song_id=0))
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_song_not_in_database_negative_value(self):
        r = requests.get(self.get_song_URL.format(song_id=-1))
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_post_with_wrong_content_type(self):
        r_post = requests.post(self.post_song_URL,
                               json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA',
                                     "released": 2013})
        location_str = r_post.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id), headers={'accept': 'text'})
        self.assertEqual(requests.codes.not_acceptable, r.status_code)

    def test_get_song_post_on_get(self):
        r = requests.post(self.get_song_URL.format(song_id=1))
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_get_song_delete_on_get_endpoint(self):
        r = requests.delete(self.get_song_URL.format(song_id=1))
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_get_song_put_on_get_endpoint(self):
        r = requests.put(self.get_song_URL.format(song_id=1))
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_get_song_large_bigger_than_int(self):
        r = requests.get(self.get_song_URL.format(song_id=343434343432432432432432))
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def threading(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA', "released": 2013})
        location_str = r.headers['Location']
        song_id = int(location_str[location_str.index('songId=') + len('songId='):])
        r = requests.get(self.get_song_URL.format(song_id=song_id))
        return r

    def test_threading(self):
        future_list = []
        with concurrent.futures.ThreadPoolExecutor() as executor:
            for i in range(0, 10):
                future_list.append(executor.submit(self.threading))
        for res in future_list:
            res = res.result()
            self.assertEqual(requests.codes.ok, res.status_code)
            json.loads(res.text)
