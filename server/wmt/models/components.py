import os
import json

from ..config import palette, site, logger


_HOOK_NAMES = set(['pre-stage', 'post-stage'])


class Error(Exception):
    pass


class IdError(Error):
    def __init__(self, id):
        self._id = id

    def __str__(self):
        return str(self._id)


def get_components():
    return palette.values()


def get_component_names(sort=False):
    names = list(palette.keys())
    if sort:
        names.sort()
    return names


def get_component(name):
    try:
        return palette[name]
    except KeyError:
        raise IdError(name)


def get_component_params(name):
    comp = get_component(name)
    return comp['parameters']


def get_component_defaults(name):
    defaults = {}

    comp = get_component(name)
    for parameter in comp['parameters']:
        defaults[parameter['key']] = parameter['value']['default']

    return defaults


def _read_input_file(name, filename):
    input_file_dir = os.path.join(site['db'], 'components', name, 'files')
    path_to_file = os.path.join(input_file_dir, filename)
    with open(path_to_file, 'r') as file:
        contents = file.read()
    return contents


def _decorate_with_header(contents, **kwds):
    header = []
    for item in kwds.items():
        header.append('%s: %s' % item)
    header_width = 80

    return os.linesep.join(
        header + [
            '-' * header_width,
            contents,
            '-' * header_width,
        ])


def get_component_input(name):
    filenames = get_component(name)['files']

    files = {}
    for (fid, filename) in enumerate(filenames):
        files[filename] = _read_input_file(name, filename)

    return files


def get_component_formatted_input(name, **kwds):
    from ..utils.templatefile import FileFormatter

    format = FileFormatter(get_component_defaults(name))

    input = dict()
    for (filename, contents) in get_component_input(name).items():
        formatted = format.format(contents, **kwds)
        (base, ext) = os.path.splitext(filename)
        if ext == '.tmpl':
            filename = base
        input[filename] = format.format(contents, **kwds)

    return input


def get_component_pretty_input(name, **kwds):
    from ..utils.templatefile import FileFormatter

    format = FileFormatter(get_component_defaults(name))

    lines = []
    for (filename, contents) in get_component_input(name).items():
        formatted = format.format(contents, **kwds)

        header = dict(file=filename)
        if len(format.missing_fields) > 0:
            header['missing'] = ', \n  '.join(format.missing_fields)
        lines.append(_decorate_with_header(formatted, **header))

        format.clear_missing()

    return os.linesep.join(lines)


def _path_to_hook(name, hook):
    """Returns the full path to the module that contains *hook*
    for component, *name*.
    """
    return os.path.join(site['db'], 'components', name, 'hooks',
                        hook + '.py')


def get_component_hooks(name):
    """Get a dictionary of all the hooks for a component.
    """
    hooks = {}
    for hook in _HOOK_NAMES:
        hooks[hook] = get_component_hook(name, hook)
    return hooks


def get_component_hook(name, hook_name):
    """Get the hook function named *hook_name*, for the component, *name*.
    """
    import imp

    pathname = _path_to_hook(name, hook_name)
    try:
        hook = imp.load_source(hook_name, pathname)
    except (IOError, ImportError):
        hook = imp.new_module(hook_name)
        def execute(*args):
            pass
        setattr(hook, 'execute', execute)

    return hook
