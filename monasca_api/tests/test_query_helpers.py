# Copyright 2015 Cray Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

import monasca_api.v2.reference.helpers as helpers

from mock import Mock
from monasca_api.v2.common.exceptions import HTTPUnprocessableEntityError

import unittest


class TestGetQueryDimension(unittest.TestCase):

    def test_no_dimensions(self):
        req = Mock()

        req.query_string = "foo=bar"

        result = helpers.get_query_dimensions(req)

        self.assertEqual(result, {})

    def test_one_dimensions(self):
        req = Mock()

        req.query_string = "foo=bar&dimensions=Dimension:Value"

        result = helpers.get_query_dimensions(req)

        self.assertEqual(result, {"Dimension": "Value"})

    def test_comma_sep_dimensions(self):
        req = Mock()

        req.query_string = ("foo=bar&"
                            "dimensions=Dimension:Value,Dimension-2:Value-2")

        result = helpers.get_query_dimensions(req)

        self.assertEqual(
            result, {"Dimension": "Value", "Dimension-2": "Value-2"})

    def test_multiple_dimension_params(self):
        req = Mock()

        req.query_string = ("foo=bar&"
                            "dimensions=Dimension:Value&"
                            "dimensions=Dimension-2:Value-2")

        result = helpers.get_query_dimensions(req)

        self.assertEqual(
            result, {"Dimension": "Value", "Dimension-2": "Value-2"})

    def test_multiple_dimension_params_with_comma_sep_dimensions(self):
        req = Mock()

        req.query_string = ("foo=bar&"
                            "dimensions=Dimension-3:Value-3&"
                            "dimensions=Dimension:Value,Dimension-2:Value-2")

        result = helpers.get_query_dimensions(req)

        self.assertEqual(
            result, {"Dimension": "Value",
                     "Dimension-2": "Value-2",
                     "Dimension-3": "Value-3"})

    def test_malformed_dimension_no_value(self):
        req = Mock()
        req.query_string = ("foo=bar&dimensions=no_value")

        self.assertRaises(
            HTTPUnprocessableEntityError, helpers.get_query_dimensions, req)

    def test_malformed_dimension_extra_colons(self):
        req = Mock()
        req.query_string = ("foo=bar&dimensions=Dimension:Value1:Value2")

        self.assertRaises(
            HTTPUnprocessableEntityError, helpers.get_query_dimensions, req)
