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
        self.get_all_song_URL = self.URL + "/songsservlet-MarvEn/songs?all"
        self.post_song_URL = self.URL + "/songsservlet-MarvEn/songs"

    def test_get_all_songs_verify_status_code(self):
        r = requests.get(self.get_all_song_URL)
        self.assertEqual(requests.codes.ok, r.status_code)

    def test_get_all_songs_content_is_json(self):
        r = requests.get(self.get_all_song_URL)
        json.loads(r.text)

    def test_get_all_songs_verify_new_length(self):
        r = requests.get(self.get_all_song_URL)
        length_before_adding_new_song = len(json.loads(r.text))
        requests.post(self.post_song_URL,
                      json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": 'RCA',
                            "released": 2013})
        r = requests.get(self.get_all_song_URL)
        length_after_adding_new_song = len(json.loads(r.text))
        self.assertTrue(length_before_adding_new_song < length_after_adding_new_song)

    def test_get_all_songs_bad_invalid_url(self):
        r = requests.get(self.URL + "/songsservlet-MarvEn/songs?al")
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_all_songs_bad_with_parameter(self):
        r = requests.get(self.URL + "/songsservlet-MarvEn/songs?all=34")
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_all_songs_bad_with_parameter_edge(self):
        r = requests.get(self.URL + "/songsservlet-MarvnEn/songs?all=")
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_get_all_songs_bad_post_on_get(self):
        r = requests.post(self.get_all_song_URL)
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_get_all_songs_bad_delete_on_get(self):
        r = requests.delete(self.get_all_song_URL)
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_get_all_songs_bad_put_on_get(self):
        r = requests.delete(self.get_all_song_URL)
        self.assertEqual(requests.codes.method_not_allowed, r.status_code)

    def test_check_content_type_of_response(self):
        r = requests.get(self.get_all_song_URL)
        self.assertTrue('application/json' in r.headers['Content-Type'])

    def threading(self):
        r = requests.get(self.get_all_song_URL)
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

