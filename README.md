# Artivact-Creator
A tool to create and organize virtual artifacts for websites or virtual experiences. 

It can be used to perform the following tasks:
- Automatically capture images via camera and artivact-turntable as input for model generation
- Remove backgrounds from captured images using rembg
- Generate 3D models using photogrammetry for single objects or in batch
- Open genrated 3D models in an editing tool to manually finalize them

# Usage
Save the provided JAR-file and run it with a JRE17 or later.

The following tools are currently supported:
- Capturing images: DigiCamControl for windows together with a custom, automated turntable
- Removing backgrounds from images: rembg
- Photogrammetry: Meshroom
- Model editing: Blender3D

All those tools must be pre-installed in order to use them. 
They can be configured as described below.

# Configuration
The following configuration parameters can be set by placing an `application.properties` file next to the program's JAR-file.

```
# possible values: RemBg
adapter.implementation.background=fallback
adapter.implementation.background.executable=
# possible values: DigiCamControl
adapter.implementation.camera=fallback
adapter.implementation.camera.executable=
# possible values: Meshroom
adapter.implementation.model-creator=fallback
adapter.implementation.model-creator.executable=
# possible values: Blender
adapter.implementation.model-editor=fallback
adapter.implementation.model-editor.executable=
# possible values: ArtivactTurntable
adapter.implementation.turntable=fallback
```