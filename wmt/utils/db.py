from __future__ import (absolute_import, print_function)

import os
from sys import stderr as STDERR

from .json import load_component


class Error(Exception):
    pass


class LoadError(Error):
    def __init__(self, msg):
        self._msg = msg

    def __str__(self):
        return self._msg


def _component_names(palette_dir):
    names = set()
    for item in os.listdir(palette_dir):
        if os.path.isdir(os.path.join(palette_dir, item)) and not item.startswith('.'):
            names.add(item)
    return names


def _load_table(name, db='.'):
    import json

    path_to_table = os.path.join(db, name + '.json')
    try:
        with open(path_to_table, 'r') as opened:
            try:
                table = json.loads(opened.read())
            except ValueError as error:
                raise LoadError(str(error))
    except IOError as error:
        print(error, file=STDERR)
        table = []
        raise LoadError(str(error))

    return table


_EMPTY_PRINT_SECTION = [
    {
        "key":"separator",
        "name":"Output",
        "description":"Output files",
        "value":{
            "type":"string",
            "default":""
        }
    }
]

_PRINT_ITEM_STRING = """
{
    "key":"${standard_name}",
    "name":"Output ${standard_name}",
    "description":"${standard_name}",
    "value":{
        "type":"choice",
        "default":"off",
        "choices":[
            "off",
            "${standard_name}"
        ]
    }
}
"""


def _print_item_as_dict(name):
    import json
    from string import Template

    s = Template(_PRINT_ITEM_STRING)
    return json.loads(s.substitute(standard_name=name))


def _construct_print_section_from_provides(path_to_db):
    import copy

    try:
        names = _load_table('provides', db=path_to_db)[0]['exchange_items']
    except (KeyError, IndexError):
        return []
    else:
        section = copy.deepcopy(_EMPTY_PRINT_SECTION)
        for name in names:
            section.append(_print_item_as_dict(name))
    return section


def _construct_component_from_db(path_to_db):
    desc = _load_table('info', db=path_to_db)

    for table in ['parameters', 'uses', 'provides', 'files', 'argv']:
        try:
            desc[table] = _load_table(table, db=path_to_db)
        except LoadError:
            if table == 'argv':
                desc[table] = []
            else:
                raise

    # print_section = _construct_print_section_from_provides(path_to_db)
    # desc['parameters'].extend(print_section)

    return desc


def load_palette(palette_dir):

    palette = {}
    for name in _component_names(palette_dir):
        path_to_db = os.path.join(palette_dir, name, 'db')
        try:
            palette[name] = _construct_component_from_db(path_to_db)
        except LoadError:
            print('%s: unable to load' % name, file=STDERR)
            raise

    return palette
