import concurrent
import os
import threading
import unittest
from concurrent.futures._base import ALL_COMPLETED

import requests


class TestSongsservlet(unittest.TestCase):

    def setUp(self):
        if os.getenv("TEST_LOCALLY") == "True":
            self.URL = "http://localhost:8080"
        else:
            self.URL = "http://82.165.236.91:8080"
        self.post_song_URL = self.URL + "/songsservlet-MarvEn/songs"

    # JUST TESTING DELETE AFTER?
    def test_good(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": "COOL Artitst", "label": "SONY", "released": 2020})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_only_title(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE"})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_title_and_released(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "released": 2020})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_all_but_title(self):
        r = requests.post(self.post_song_URL,
                          json={"artist": "COOL Artitst", "label": "SONY", "released": 2020})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_extra_key(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": "COOL Artitst", "label": "SONY", "released": 2020,
                                "randomAssKey": "420"})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_empty_json(self):
        r = requests.post(self.post_song_URL,
                          json={})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_only_garbage(self):
        r = requests.post(self.post_song_URL,
                          json={"randomAssKey": "420"})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_wrong_key_type(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": "COOL Artitst", "label": "SONY", "released": "2020"})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_wrong_key_type_2(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": 123, "label": "SONY", "released": "2020"})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_less_keys(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": "COOL Artitst"})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_less_keys(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "released": 2020})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_less_keys_2(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "label": "SONY", "released": 2020})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_less_keys_3(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "label": "SONY"})
        self.assertEqual(requests.codes.created, r.status_code)

    # How should we deal with this?
    def test_empty_value(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "SONG_TITLE", "artist": "", "label": "", "released": 2020})
        self.assertEqual(requests.codes.created, r.status_code)

    # END

    def test_post_with_content(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_post_with_content_check_response_text(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013})
        self.assertTrue('/songsservlet-MarvEn/songs?songId=' in r.headers['Location'])

    def test_post_with_another_order(self):
        r = requests.post(self.post_song_URL,
                          json={"label": "RCA", "released": 2013, "title": "Wrecking Ball", "artist": "MILEYCYRUS"})
        self.assertTrue('/songsservlet-MarvEn/songs?songId=' in r.headers['Location'])

    def test_post_without_content(self):
        r = requests.post(self.post_song_URL)
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_post_with_good_content_key_missing(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "label": "RCA", "released": 2013})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_post_with_bad_content_released_no_int(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": '2016'})
        self.assertEqual("A Value has bad format", r.text)

    def test_post_with_bad_content_string_not_string(self):
        r = requests.post(self.post_song_URL,
                          json={"title": 1, "artist": "MILEYCYRUS", "label": "RCA", "released": 2013})
        self.assertEqual(requests.codes.bad_request, r.status_code)
        self.assertEqual("A Value has bad format", r.text)

    def test_post_with_content_empty_value(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "", "label": "RCA", "released": 2013})
        self.assertEqual(requests.codes.created, r.status_code)

    def test_post_with_bad_content_released_not_castable(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": '20de16'})
        self.assertEqual("A Value has bad format", r.text)

    def test_post_wrong_url(self):
        r = requests.post(self.post_song_URL + 's')
        self.assertEqual(requests.codes.not_found, r.status_code)

    def test_post_edge_with_more_keys(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013,
                                "author": "test"})
        self.assertEqual(requests.codes.bad_request, r.status_code)

    def test_post_edge_with_wrong_content_type(self):
        r = requests.post(self.post_song_URL,
                          data='{"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2020}, '
                               '"released": 2020}')
        self.assertEqual(requests.codes.unsupported_media_type, r.status_code)

    def test_get_on_post_endpoint(self):
        r = requests.get(self.post_song_URL)
        self.assertEqual(requests.codes.not_allowed, r.status_code)

    def test_delete_on_post_endpoint(self):
        r = requests.delete(self.post_song_URL)
        self.assertEqual(requests.codes.not_allowed, r.status_code)

    def test_put_on_post_endpoint(self):
        r = requests.put(self.post_song_URL)
        self.assertEqual(requests.codes.not_allowed, r.status_code)

    def test_post_with_wrong_content_type(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013},
                          headers={'content-type': 'text/html'})
        self.assertEqual(requests.codes.unsupported_media_type, r.status_code)

    def test_post_bad_accept_header(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013},
                          headers={'accept': 'xml'})
        self.assertEqual(requests.codes.not_acceptable, r.status_code)

    def threading(self):
        r = requests.post(self.post_song_URL,
                          json={"title": "Wrecking Ball", "artist": "MILEYCYRUS", "label": "RCA", "released": 2013})
        return r

    def test_threading(self):
        future_list = []
        with concurrent.futures.ThreadPoolExecutor() as executor:
            for i in range(0, 10):
                future_list.append(executor.submit(self.threading))
        for res in future_list:
            res = res.result()
            self.assertEqual(requests.codes.created, res.status_code)
            self.assertTrue('/songsservlet-MarvEn/songs?songId=' in res.headers['Location'])

