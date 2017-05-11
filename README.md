# Requirements
* I develop with **Unity3D 5.6** and tested it on my **Android 6.0** based phone, not sure with other versions or platforms.
* The shader is written to target **GLSL ES 3.2**, as far as I know, other versions of GLSL ES do not support SamplerBuffer.

# Known Issues
* Pay attention about the **GL_MAX_TEXTURE_BUFFER_SIZE_EXT** value. The number of texels in the texel array is then clamped to the value of the implementation-dependent limit MAX_TEXTURE_BUFFER_SIZE_EXT. When a buffer texture is accessed in a shader, the results of a texel fetch are undefined if the specified texel coordinate is negative, or greater than or equal to the clamped number of texels in the texel array. More details could find in [EXT_texture_buffer](https://www.khronos.org/registry/OpenGL/extensions/EXT/EXT_texture_buffer.txt) and [wiki](https://www.khronos.org/opengl/wiki/Buffer_Texture).
* **Don't** use Texture2D.CreateExternalTexture which will generate an OpenGL error, because it will try to bind the Texture ID to Texture2D. And if you use a wrong shader which regard the texture as Texture2D will cause the same problem, for example, use Unity standard surface shader or use a sampler2D. Remeber, **only samplerBuffer could be used for Buffer Texture**.
* Even though [Unity3D support pure GLSL shader](https://docs.unity3d.com/Manual/SL-GLSLShaderPrograms.html), but the documentation is very poor and it takes me hours to figure it out how to let unity compiled shader target GLSL ES 3.2.
* In GLSL, only **highp int** or **highp uint** use 32-bits, others will use implementation-dependent numbers of bits which is 16 bits on my test phone. So if you need large numbers, make sure you use **highp int** to avoid overflow. And I really recommend to read [GLSL_ES_Specification_3.20](https://www.khronos.org/registry/OpenGL/specs/es/3.2/GLSL_ES_Specification_3.20.pdf) if you want to write your onw shader.
* I didn't figure out a way to use multiple Buffer Texture, so **ONLY ONE** texture could be used!

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
