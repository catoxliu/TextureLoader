Shader "Unlit/TextureBufferShader"
{
	Properties
	{
		_MainTex ("Texture", 2D) = "white" {}
	}
	SubShader
	{
		Tags { "RenderType"="Opaque" }
		LOD 100

		Pass
		{

			GLSLPROGRAM
			#extension GL_OES_EGL_image_external_essl3 : require
			//#extension GL_OES_EGL_image_external : require
			//#extension EXT_gpu_shader4 : enable
			//#extension GL_OES_texture_buffer : require

			#include "UnityCG.glslinc"
			#include "GLSLSupport.glslinc"
			
			#ifdef VERTEX
			#version 320 es

			in vec4 _glesVertex;
			in vec4 _glesMultiTexCoord0;
			out vec2 TextureCoordinate;
			
			void main()
			{
				gl_Position = gl_ModelViewProjectionMatrix * _glesVertex;
				TextureCoordinate = _glesMultiTexCoord0.st;
			}
			
			#endif
			
			#ifdef FRAGMENT
			#version 320 es

			uniform highp usamplerBuffer _MainTex;
			in vec2 TextureCoordinate;
			layout(location = 0) out mediump vec4 _glesFragColor;

			void main()
			{
				highp int width = int(sqrt(float(textureSize(_MainTex))));
				float scaler = float(width) - 1.0;
				highp int coordx = int(TextureCoordinate.x * scaler);
				highp int coordy = int(TextureCoordinate.y * scaler);
				highp int index = (coordx + (coordy * width));
				uvec4 color = texelFetch(_MainTex, index);
				/*uvec4 color = uvec4(0.0, 0.0, 1.0, 0.0);
				if (coordy < width && coordy > 0)
					if (index < textureSize(_MainTex) && index > 0)
						color = texelFetch(_MainTex, index);
					else
						color = uvec4(0.0, 255.0, 0.0, 0.0);
				else
					color = uvec4(255.0, 0.0, 0.0, 0.0);*/
				
				_glesFragColor.r = float(color.r) / 255.0;
				_glesFragColor.g = float(color.g) / 255.0;
				_glesFragColor.b = float(color.b) / 255.0;
				_glesFragColor.a = float(color.a) / 255.0;
			}
			
			#endif
			
			ENDGLSL
		}
	}
}
