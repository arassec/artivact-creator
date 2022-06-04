import bpy
import os
import sys
from mathutils import *

MODEL_DIR=sys.argv[4]

# get list of all files in directory
file_list = os.listdir(MODEL_DIR)

# get a list of files ending in 'obj'
obj_list = [item for item in file_list if item[-3:] == 'obj']

# loop through the strings in obj_list
for item in obj_list:
    # select all object
    bpy.ops.object.select_all(action='SELECT')

    # delete selected
    bpy.ops.object.delete(use_global=True)

    # import obj
    path_to_import = os.path.join(MODEL_DIR, item)
    bpy.ops.import_scene.obj(filepath = path_to_import)

    # rename
    for obj in bpy.context.selected_objects:
        obj.name = item[:-4]
        obj.rotation_euler = Vector([45, 135, 0])
        bpy.ops.object.origin_set(type='GEOMETRY_ORIGIN', center='MEDIAN')
        print(dir(obj))

    # write blend
    path_to_export = os.path.join(MODEL_DIR, item[:-4] + ".blend")
    print(path_to_export)
    bpy.ops.wm.save_as_mainfile(filepath=path_to_export, check_existing=False )
