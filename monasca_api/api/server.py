# Copyright 2014 IBM Corp
# Copyright 2015 Hewlett-Packard
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.

import os
from wsgiref import simple_server

import falcon
from oslo_config import cfg
from oslo_log import log
import paste.deploy
import simport

dispatcher_opts = [cfg.StrOpt('versions', default=None,
                              help='Versions'),
                   cfg.StrOpt('metrics', default=None,
                              help='Metrics'),
                   cfg.StrOpt('metrics_measurements', default=None,
                              help='Metrics measurements'),
                   cfg.StrOpt('metrics_statistics', default=None,
                              help='Metrics statistics'),
                   cfg.StrOpt('metrics_names', default=None,
                              help='Metrics names'),
                   cfg.StrOpt('alarm_definitions', default=None,
                              help='Alarm definitions'),
                   cfg.StrOpt('alarms', default=None,
                              help='Alarms'),
                   cfg.StrOpt('alarms_state_history', default=None,
                              help='Alarms state history'),
                   cfg.StrOpt('notification_methods', default=None,
                              help='Notification methods')]

dispatcher_group = cfg.OptGroup(name='dispatcher', title='dispatcher')
cfg.CONF.register_group(dispatcher_group)
cfg.CONF.register_opts(dispatcher_opts, dispatcher_group)

LOG = log.getLogger(__name__)


def launch(conf, config_file="/etc/monasca/api-config.conf"):
    log.register_options(cfg.CONF)
    log.set_defaults()
    cfg.CONF(args=[],
             project='monasca_api',
             default_config_files=[config_file])
    log.setup(cfg.CONF, 'monasca_api')

    app = falcon.API()

    versions = simport.load(cfg.CONF.dispatcher.versions)()
    app.add_route("/", versions)
    app.add_route("/{version_id}", versions)

    metrics = simport.load(cfg.CONF.dispatcher.metrics)()
    app.add_route("/v2.0/metrics", metrics)

    metrics_measurements = simport.load(
        cfg.CONF.dispatcher.metrics_measurements)()
    app.add_route("/v2.0/metrics/measurements", metrics_measurements)

    metrics_statistics = simport.load(cfg.CONF.dispatcher.metrics_statistics)()
    app.add_route("/v2.0/metrics/statistics", metrics_statistics)

    metrics_names = simport.load(cfg.CONF.dispatcher.metrics_names)()
    app.add_route("/v2.0/metrics/names", metrics_names)

    alarm_definitions = simport.load(cfg.CONF.dispatcher.alarm_definitions)()
    app.add_route("/v2.0/alarm-definitions/", alarm_definitions)
    app.add_route("/v2.0/alarm-definitions/{alarm_definition_id}",
                  alarm_definitions)

    alarms = simport.load(cfg.CONF.dispatcher.alarms)()
    app.add_route("/v2.0/alarms", alarms)
    app.add_route("/v2.0/alarms/{alarm_id}", alarms)

    alarms_state_history = simport.load(
        cfg.CONF.dispatcher.alarms_state_history)()
    app.add_route("/v2.0/alarms/state-history", alarms_state_history)
    app.add_route("/v2.0/alarms/{alarm_id}/state-history",
                  alarms_state_history)

    notification_methods = simport.load(
        cfg.CONF.dispatcher.notification_methods)()
    app.add_route("/v2.0/notification-methods", notification_methods)
    app.add_route("/v2.0/notification-methods/{notification_method_id}",
                  notification_methods)

    LOG.debug('Dispatcher drivers have been added to the routes!')
    return app


if __name__ == '__main__':
    wsgi_app = (
        paste.deploy.loadapp('config:etc/api-config.ini',
                             relative_to=os.getcwd()))
    httpd = simple_server.make_server('127.0.0.1', 8080, wsgi_app)
    httpd.serve_forever()
