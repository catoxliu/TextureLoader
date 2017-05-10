# New Branch
I just upload a new branch "texture_buffer" which used the new Opengl ES 3.1 feature [EXT_texture_buffer](https://www.khronos.org/registry/OpenGL/extensions/EXT/EXT_texture_buffer.txt) to do direct CPU writes (MapBuffer).

# TextureLoader
This is a Unity3D native plugin for Android platform to load an external image file using OpenGL ES.

We are developing VR app on our all-in-one HMD** "Idealens K2" **and loading external image file when running is a very common demand here. **Since Unity's LoadImage will cause intolerable latency due to generating new GPU resources, I made this native plugin to load image files by myself.** It becomes quite smooth using this plugin to update a 1024*1024 Texture.

The key idea here is to prepare GPU resources (generate Texture2Ds) at the init state and when it need to load a image a seperate thread will read file and update GPU resource (generated before) as soon as file reading finishes.

# Usage
All you need is in **TextureLoaderPlugin.cs**.

# Functions
* **Init()**
Call this at the very beginning of your scene. This should be called only once in your project.
* **InitTexture(TextureCount, TextureSize, InitImagePath)**
This is used to prepare GPU resources (generate Texture2Ds). TextureCount means how many Texture2Ds do you want and TextureSize will be default if set to 0. InitImagePath could be empty and you will get a default Texture2D (black).
* **UpdateTexture(imagePath, imageSize, TextureID)**
This is used to load an Image file to one of your Texture2Ds generated above. imageSize must be the same to what you set in InitTexture, 0 means default value will be used. TextureID is the native resource ID you could get after InitTexture generate Texture2Ds correctly.

# Callbacks
* **InitTexturesCallback**
This will be called once the Texture2Ds generated correctly and native resource IDs could be reach through InitTextureIDList[].
* **UpdateTextureFinish(TextureID)**
This will be called once the update GPU resourc finish. If update successfully, TextureID will be the same as UpdateTexture's call, otherwise, the update failed.
