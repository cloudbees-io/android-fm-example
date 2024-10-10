
# Example Kotlin Android application for CloudBees platform feature management
Use this example application to integrate with the CloudBees platform and test out feature management. After integrating, watch the application display change in response to any updates you make to flag values in the platform.

In the example kotlin application, the ROX SDK is already set up, and feature flags are already coded in.


## Running This Project
To get started with the android-fm-example project, follow these steps:

1. **Get key from Cloudbees account:** 

    - Create a CloudBees Feature Management account. See [Signup Page](https://cloudbees.io/signup) to create an account.
    - Get your environment key. Copy your environment key from App settings > Environments > Key.

2. **Clone the Repository:** 
Clone the android-fm-example repository to your local machine using Git:

```shell
git clone git@github.com:cloudbees-io/android-fm-example.git
```
3. **Install ROX dependancy:**
   - Add the following in the dependencies block of your build.gradle file:
   `implementation("io.rollout.rox:rox-android:5.0.5")`
   - After adding dependancy Sync project
 

4. **Open the Project:**
 
    - Reopen your project in Android studio.

5. **Setup key from Cloudbees account:** 

    - In the AndroidManifest.xml file, replace the `<Your Cloudbees Environment API Key>` with your corresponding API key:
   
    ```
        <meta-data
            android:name="rox.apiKey"
            android:value="<Your Cloudbees Environment API Key>" />
    ```

6. **Run the android-fm-example App:** 

    - The flag name is automatically added to your cloudbees dashboard after running the application.

## Use the platform to update flag values

Now that your application is running, go to your environment in Feature management to display the flags available in the example application:

Table 1. Feature flags in the example application.

| Flag name           | Flag type  | Description                    |
|---------------------|------------|--------------------------------|
| `showMessage`| Boolean | Turns the message show or hide |
| `message`| String | Sets the Message string.|
| `fontColor`| String | Sets the font color. The flag value has the following variations: red, green,yellow or blue.|
| `fontSize` | Int32   | Sets the font size in pixels. The flag value has the following variations: 12, 14, or 18.|
| `specialNumber` | Double   | Sets the number with double. The flag value has the following variations: 2.72, 0.577, 3.14|

**To update flags in the platform UI:** 

1. Select **Feature management** from the left pane.
2. Select the vertical ellipsis icon next to the flag you want to configure.
3. Select **Configure**.
4. Select the **Environment** you used for copying the SDK key.
5. Update a flag value and save your changes.
6. Switch the **Configuration status** to **On**.

## Video Preview

[![Video Preview](assets/fm-android-thumb.jpg)](assets/fm-screen-rec.mov)
