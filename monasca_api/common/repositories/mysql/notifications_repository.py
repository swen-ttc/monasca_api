# Copyright 2014 Hewlett-Packard
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

import datetime

from oslo_log import log
from oslo_utils import uuidutils

from monasca_api.common.repositories import exceptions
from monasca_api.common.repositories.mysql import mysql_repository
from monasca_api.common.repositories import notifications_repository as nr

LOG = log.getLogger(__name__)


class NotificationsRepository(mysql_repository.MySQLRepository,
                              nr.NotificationsRepository):
    def __init__(self):

        super(NotificationsRepository, self).__init__()

    def create_notification(self, tenant_id, name,
                            notification_type, address):

        cnxn, cursor = self._get_cnxn_cursor_tuple()

        with cnxn:
            query = """
                select *
                from notification_method
                where tenant_id = %s and name = %s"""

            parms = [tenant_id, name.encode('utf8')]

            cursor.execute(query, parms)

            if cursor.rowcount > 0:
                raise exceptions.AlreadyExistsException('Notification already '
                                                        'exists')

            now = datetime.datetime.utcnow()
            notification_id = uuidutils.generate_uuid()
            query = """
                insert into notification_method(
                  id,
                  tenant_id,
                  name,
                  type,
                  address,
                  created_at,
                  updated_at
                ) values (%s, %s, %s, % s, %s, %s, %s)"""

            parms = [notification_id,
                     tenant_id,
                     name.encode('utf8'),
                     notification_type.encode('utf8'),
                     address.encode('utf8'),
                     now,
                     now]

            cursor.execute(query, parms)

        return notification_id

    @mysql_repository.mysql_try_catch_block
    def list_notifications(self, tenant_id, offset, limit):

        query = """
            select *
            from notification_method
            where tenant_id = %s"""

        parms = [tenant_id]

        if offset:
            query += " and id > %s "
            parms.append(offset.encode('utf8'))

        query += " order by id limit %s "
        parms.append(limit + 1)

        rows = self._execute_query(query, parms)

        return rows

    @mysql_repository.mysql_try_catch_block
    def delete_notification(self, tenant_id, id):

        cnxn, cursor = self._get_cnxn_cursor_tuple()

        with cnxn:
            query = """
                select *
                from notification_method
                where tenant_id = %s and id = %s"""

            parms = [tenant_id, id]

            cursor.execute(query, parms)

            if cursor.rowcount < 1:
                raise exceptions.DoesNotExistException

            query = """
                delete
                from notification_method
                where tenant_id = %s and id = %s"""

            cursor.execute(query, parms)

    @mysql_repository.mysql_try_catch_block
    def list_notification(self, tenant_id, notification_id):

        parms = [tenant_id, notification_id]

        query = """
                select *
                from notification_method
                where tenant_id = %s and id = %s"""

        rows = self._execute_query(query, parms)

        if rows:
            return rows[0]
        else:
            raise exceptions.DoesNotExistException

    @mysql_repository.mysql_try_catch_block
    def find_notification_by_name(self, tenant_id, name):
        cnxn, cursor = self._get_cnxn_cursor_tuple()
        parms = [tenant_id, name]

        with cnxn:
            query = """
                select *
                from notification_method
                where tenant_id = %s and name = %s
                """
            rows = self._execute_query(query, parms)

            if rows:
                return rows[0]
            else:
                return None

    @mysql_repository.mysql_try_catch_block
    def update_notification(
            self, id, tenant_id, name, type, address):

        cnxn, cursor = self._get_cnxn_cursor_tuple()

        with cnxn:
            now = datetime.datetime.utcnow()

            query = """
                update notification_method
                set name = %s,
                    type = %s,
                    address = %s,
                    created_at = %s,
                    updated_at = %s
                 where tenant_id = %s and id = %s"""

            parms = [name.encode('utf8'), type.encode('utf8'), address.encode(
                'utf8'), now, now, tenant_id, id]

            cursor.execute(query, parms)

            if cursor.rowcount < 1:
                raise exceptions.DoesNotExistException('Not Found')
