using UnityEngine;
using System.Collections;

public class CubeTest : MonoBehaviour {

    public Transform mCube1;
    public Transform mCube2;

    string initImagePath; //use this file to init the texture, black if it's not set
    string updateImagePath1 = @"/sdcard/Pictures/test1.jpg";
    string updateImagePath2 = @"/sdcard/Pictures/test2.jpg";

    bool initTexture = false;
    float timeCounter = 0;

    void Awake()
    {
        TextureLoaderPlugin.Init();
    }

	// Use this for initialization
	void Start () {
        StartCoroutine(RollCube());
        TextureLoaderPlugin.InitTexture(2, 0, initImagePath);
    }
	
	// Update is called once per frame
	void Update () {
        timeCounter += Time.unscaledDeltaTime;
        if (!initTexture && TextureLoaderPlugin.IsInitTexturesFinish)
        {
            initTexture = true;
            Texture2D t1 = Texture2D.CreateExternalTexture(512, 512, TextureFormat.ARGB4444, false, false, (System.IntPtr)TextureLoaderPlugin.InitTextureIDList[0]);
            mCube1.GetComponent<MeshRenderer>().material.mainTexture = t1;
            Texture2D t2 = Texture2D.CreateExternalTexture(512, 512, TextureFormat.ARGB4444, false, false, (System.IntPtr)TextureLoaderPlugin.InitTextureIDList[1]);
            mCube2.GetComponent<MeshRenderer>().material.mainTexture = t2;
        }
        else if (timeCounter > 1)
        {
            timeCounter = 0;
            int s = (int)Time.unscaledTime % 4;
            switch (s)
            {
                case 0:
                case 1:
                    TextureLoaderPlugin.UpdateTexture(updateImagePath1, 0, TextureLoaderPlugin.InitTextureIDList[s % 2]);
                    break;
                case 2:
                case 3:
                    TextureLoaderPlugin.UpdateTexture(updateImagePath2, 0, TextureLoaderPlugin.InitTextureIDList[s % 2]);
                    break;
            }
        }
	}

    IEnumerator RollCube()
    {
        while (true)
        {
            mCube1.Rotate(Vector3.up, 0.2f, Space.World);
            mCube2.Rotate(Vector3.up, 0.2f, Space.World);
            yield return null;
        }
    }
}
