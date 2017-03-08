using UnityEngine;
using System.Collections;
using System.Runtime.InteropServices;
using System;

public class TextureLoaderPlugin
{

    public enum TextureLoaderStatus
    {
        TLS_NOT_INIT = 0,       //native library is not loaded yet.
        TLS_INIT_FAIL = 1,      //native library is loading or not init properly.
        TLS_IDLE = 2,           //native library is ready to be used.
        TLS_LOADING_FILE = 3,   //the image file is loading in another thread.
        TLS_LOAD_FILE_SUC = 4,  //the image file is ready to be upload to texture
        TLS_UPDATING_TEX = 5,   //render thread is updating image file to texture
        TLS_UNKNOWN = 6,        //plugin messed up.
    }

    #region external Native Func
    [DllImport("TextureLoader")]
    private static extern IntPtr GetRenderEventFunc();
    [DllImport("TextureLoader")]
    private static extern void StartLoadImageFile(string path, int imageSize);
    [DllImport("TextureLoader")]
    private static extern int GetTextureLoadStatus();
    [DllImport("TextureLoader")]
    private static extern void GetInitTextureID([MarshalAs(UnmanagedType.LPArray, SizeParamIndex = 1)] int[] initTexIdList);
    [DllImport("TextureLoader")]
    private static extern void SetInitTextureCountAndSize(int texCount, int imageSize);
    [DllImport("TextureLoader")]
    private static extern bool IsInitTextureFinish();
    #endregion //external Native Func

    #region Local Member
    private static IntPtr pNativeRenderFunc = IntPtr.Zero;
    private static TextureLoaderStatus eCurrentStatus = TextureLoaderStatus.TLS_NOT_INIT;
    private static TextureLoaderMB mMonoBehaviour = null;
    private static int[] iInitTexIDList = null;
    private static bool bInitTexFinish = false;
    #endregion //Local Member

    #region Property
    /// <summary>
    /// Normally, there is no need for devs to concern this status.
    /// But could be useful to debug when the plugin messed up.
    /// </summary>
    public static TextureLoaderStatus CurrentStatus
    { get { return eCurrentStatus; } }

    /// <summary>
    /// Only store ids for the very last InitTexture call.
    /// Better to check if the values are 0 if glGenTextures fails.
    /// </summary>
    public static int[] InitTextureIDList
    { get { return iInitTexIDList; } }

    /// <summary>
    /// Turns true if the very last InitTexture call finish.
    /// Just finish, no meaning of success.
    /// </summary>
    public static bool IsInitTexturesFinish
    { get { return bInitTexFinish; } }
    #endregion //Property

    #region Public Static Func
    /// <summary>
    /// Must be called before use any other methods of this plugin.
    /// Create a TextureLoaderMB in the scene for loop update.
    /// </summary>
    public static void Init()
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (mMonoBehaviour == null)
        {
            GameObject go = GameObject.Instantiate(new GameObject());
            go.name = "TextureLoader";
            mMonoBehaviour = go.AddComponent<TextureLoaderMB>();
        }
#else
        Debug.LogWarning("This could only be used on Android!");
#endif
    }

    /// <summary>
    /// This is used to generate particular textures in native.
    /// Textures id could be reach through property InitTextureIDList 
    /// when IsInitTexturesFinish is true
    /// Once invoke this method this set IsInitTexturesFinish to false
    /// and InitTextureIDList to a new List, so if there is need to init 
    /// textures several times, texture ids should be saved in other places.
    /// </summary>
    /// <param name="TextureCount">
    /// How many textures do you want to generate. Default value 0 means 1 texture
    /// </param>
    /// <param name="TextureSize">
    /// What size do you want to set these textures to. Default value 0 means 512.
    /// This must be power of 2 due to the limitation of opengles.
    /// </param>
    /// <param name="path">
    /// The image file that you want to use to init these textures.
    /// Null or empty will just make black textures;
    /// </param>
    /// <returns>Whether if this method execute successfully.
    /// If this plugin not Init properly or TextureLoaderMB not in scene, 
    /// this will return false
    /// </returns>
    public static bool InitTexture(int TextureCount = 0, int TextureSize = 0, string path = "")
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (mMonoBehaviour == null) return false;
        bInitTexFinish = false;
        if (TextureCount == 0) TextureCount = 1;
        if (TextureSize == 0) TextureSize = 512;
        iInitTexIDList = new int[TextureCount];
        SetInitTextureCountAndSize(TextureCount, TextureSize);
        if (!string.IsNullOrEmpty(path)) StartLoadImageFile(path, TextureSize);
        mMonoBehaviour.InitTexture();
        return true;
#else
        Debug.LogWarning("This could only be used on Android!");
        return false;
#endif
    }

    /// <summary>
    /// Update the particular texture with the image file path in native
    /// Only one texture could be updated at the same time for performance consider.
    /// </summary>
    /// <param name="path">The image file path that used to update</param>
    /// <param name="imageSize">
    /// This must be exactly the same as the texture init size!
    /// Default value 0 means use the size of the last InitTexture's call.
    /// </param>
    /// <param name="TextureID">
    /// The texture id (not the index) that used for update.
    /// Default Value 0 means use iInitTexIDList first id, pay attention,
    /// if more than one textures need to be updated, it's better to manage
    /// texture ids somewhere else.
    /// </param>
    /// <returns>Whether if this method execute successfully.
    /// If this plugin not Init properly or TextureLoaderMB not in scene, 
    /// or the last update call didn't finish yet, or be generating textures,
    /// this will return false
    /// </returns>
    public static bool UpdateTexture(string path, int imageSize = 0, int TextureID = 0)
    {
#if UNITY_ANDROID && !UNITY_EDITOR
        if (mMonoBehaviour == null || eCurrentStatus != TextureLoaderStatus.TLS_IDLE || !bInitTexFinish) return false;
        StartLoadImageFile(path, imageSize);
        if (TextureID == 0) TextureID = iInitTexIDList[0];
        mMonoBehaviour.UpdateTexture(TextureID);
        return true;
#else
        Debug.LogWarning("This could only be used on Android!");
        return false;
#endif
    }
    #endregion //Public Static Func

    #region Private Func
    /// <summary>
    /// Native call to Render thread!
    /// </summary>
    /// <param name="EventID">0 means to init;
    /// Bigger than 0 means the texture id needs to update</param>
    private static void SendRenderEvent(int EventID)
    {
        if (pNativeRenderFunc == IntPtr.Zero)
        {
            pNativeRenderFunc = GetRenderEventFunc();
        }
        Debug.Log("SendRenderEvent " + EventID);
        GL.IssuePluginEvent(pNativeRenderFunc, EventID);
    }
    #endregion //Private Func

    #region Internal Monobehaviour
    /// <summary>
    /// This is used as a main loop for update plugin status and to call SendRenderEvent.
    /// </summary>
    internal class TextureLoaderMB : MonoBehaviour
    {

        private int currentTask = -1; //-1-nothing; 0-init texture; other-update texture

        void Awake()
        {
            DontDestroyOnLoad(gameObject);
        }

        void Start()
        {
            StartCoroutine(MainLoop());
        }

        private IEnumerator MainLoop()
        {
            while (true)
            {
                // It will be better to not run this at the end of a frame.
                yield return new WaitForEndOfFrame();
                eCurrentStatus = (TextureLoaderStatus)GetTextureLoadStatus();
                if (currentTask == 0 && (eCurrentStatus == TextureLoaderStatus.TLS_IDLE || eCurrentStatus == TextureLoaderStatus.TLS_LOAD_FILE_SUC))
                {
                    SendRenderEvent(0);
                    currentTask = -1;
                }
                else if (currentTask > 0 && eCurrentStatus == TextureLoaderStatus.TLS_LOAD_FILE_SUC)
                {
                    SendRenderEvent(currentTask);
                    currentTask = -1;
                }
                if (!bInitTexFinish && IsInitTextureFinish())
                {
                    bInitTexFinish = true;
                    GetInitTextureID(iInitTexIDList);
                }
                //Debug.Log("Current Status " + eCurrentStatus + " " + currentTask);
            }
        }

        public void InitTexture()
        {
            currentTask = 0;
        }

        public void UpdateTexture(int texID)
        {
            if (texID == 0)
            {
                Debug.LogWarning("Texture ID could not be 0!");
                return;
            }
            currentTask = texID;
        }

    }
    #endregion //Internal Monobehaviour
}
