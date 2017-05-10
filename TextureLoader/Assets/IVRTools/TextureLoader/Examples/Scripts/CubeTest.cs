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
    int iLeftTexID = 0, iRightTexID = 0;
    int iCurrentUpdateTexID;

    void Awake()
    {
        TextureLoaderPlugin.Init();
    }

	// Use this for initialization
	void Start () {
        StartCoroutine(RollCube());
        TextureLoaderPlugin.InitTexturesCallback += InitTextureFinish;
        TextureLoaderPlugin.UpdateTextureCallback += UpdateTextureFinish;
        TextureLoaderPlugin.InitTexture(2, 4096, initImagePath);
    }

    void InitTextureFinish()
    {
        Debug.Log("Init Texture Finish!");
        initTexture = true;
        iLeftTexID = TextureLoaderPlugin.InitTextureIDList[0];
        iRightTexID = TextureLoaderPlugin.InitTextureIDList[1];
        //Texture2D t1 = Texture2D.CreateExternalTexture(512, 512, TextureFormat.ARGB4444, false, false, (System.IntPtr)iLeftTexID);
        Texture2D t1 = new Texture2D(4096, 4096, TextureFormat.RGBAFloat, false, false);
        t1.UpdateExternalTexture((System.IntPtr)iLeftTexID);
        mCube1.GetComponent<MeshRenderer>().material.mainTexture = t1;
        //Texture2D t2 = Texture2D.CreateExternalTexture(512, 512, TextureFormat.ARGB4444, false, false, (System.IntPtr)iRightTexID);
        Texture2D t2 = new Texture2D(4096, 4096, TextureFormat.RGBAFloat, false, false);
        t2.UpdateExternalTexture((System.IntPtr)iRightTexID);
        mCube2.GetComponent<MeshRenderer>().material.mainTexture = t2;

    }

    void UpdateTextureFinish(int texID)
    {
        Debug.Log("Update Texture ["+iCurrentUpdateTexID+"] Finish with " + texID);
        if (texID == -1)
        {
            Debug.Log(iCurrentUpdateTexID == iLeftTexID ? "Load Left Cube Image Fail" : "Load Right Cube Image Fail");
        }
        else if (texID == 0)
        {
            Debug.Log(iCurrentUpdateTexID == iLeftTexID ? "Update Left Cube Fail" : "Update Right Cube Fail");
        }
        else if (iCurrentUpdateTexID == texID)
        {
            Debug.Log(texID == iLeftTexID ? "Update Left Cube Success" : "Update Right Cube Success");
        }
    }

    // Update is called once per frame
    void Update () {
        timeCounter += Time.unscaledDeltaTime;
        if (initTexture && timeCounter > 1)
        {
            if (TextureLoaderPlugin.CurrentStatus != TextureLoaderPlugin.TextureLoaderStatus.TLS_IDLE) return;
            timeCounter = 0;
            int s = (int)Time.unscaledTime % 4;
            iCurrentUpdateTexID = (s % 2 == 0 ? iLeftTexID : iRightTexID);
            string updatePath = (s < 2) ? updateImagePath1 : updateImagePath2;
            if (!TextureLoaderPlugin.UpdateTexture(updatePath, 4096, iCurrentUpdateTexID))
            {
                Debug.LogWarning("Could not update texture right now!");
                iCurrentUpdateTexID = 0;
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
