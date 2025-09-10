
# Example Kotlin Android application for CloudBees platform feature management
Use this example application to integrate with the CloudBees platform and test feature management. After integrating, observe the application display change in response to any updates you make to flag values in the platform.

In the example Kotlin application, the ROX SDK is already set up, and feature flags are already coded in.

## Get started with this project
To get started with the android-fm-example project, follow these steps:

1. **Clone the Repository:**  
   Clone the android-fm-example repository to your local machine using Git:

```shell
git clone git@github.com:cloudbees-io/android-fm-example.git
```

2. **Open the Project:**

    - Reopen your project in Android Studio.

3. **Get the SDK key:**
    - Create a CloudBees feature management account. Refer to [Signup Page](https://cloudbees.io/signup) to create an account.
    - Locate and copy your SDK key:  
          -- Navigate to **Feature management > Flags**.    
          -- Select an application.    
          -- Select the **copy** button next to the SDK key on the page.  
    - If you do not see an SDK key:    
          -- Navigate to **Feature management > Flags**.  
          -- Select **Installation instructions** in the upper right corner.  
          -- Follow the installation instructions.  
          -- Close the installation instructions, you may now copy the SDK Key.  

4. **Open the Project:**
 
    - Reopen your project in Android Studio.  

5. **Add the SDK Key to your Cloudbees account:** 

    - In the AndroidManifest.xml file, replace the `<Your Cloudbees SDK Key>` with your corresponding API key:  
   
    ```
        <meta-data
            android:name="rox.apiKey"
            android:value="<Your Cloudbees SDK Key>" />
    ```

6. **Run the android-fm-example App:** 

   * Select the run button in Android Studio.    
   * After running the application, the flag name is automatically added to your CloudBees dashboard.  

## Use the platform to update flag values

Now that your application is running:  
* In CloudBees platform, select **Feature management**.   
* Select your example application to display the available flags as shown in the table below.  

Table 1. Feature flags in the example application.  

| Flag name           | Flag type  | Description                    |
|---------------------|------------|--------------------------------|
| `showMessage`| Boolean | Turns the message show or hide |
| `message`| String | Sets the Message string.|
| `fontColor`| String | Sets the font color. The flag value has the following variations: red, green, yellow, or blue.|
| `fontSize` | Int32   | Sets the font size in pixels. The flag value has the following variations: 12, 14, or 18.|
| `specialNumber` | Double   | Sets the number with double. The flag value has the following variations: 2.72, 0.577, 3.14|

**To update flags in the platform UI:**   

1. Select **Feature management** from the left pane.  
2. Select the application.  
3. Select the vertical ellipsis next to the flag you want to configure.  
4. Select **Configure**.  
5. Select the **Environment** for the SDK key.  
6. Update a flag value and save your changes.  
7. Set the **Configuration status** to **On**.  

**Use the application with multiple SDK keys:**

Run multiple instances of the SDK in a single application, each with its own SDK key and environment. Each instance is fully isolated. Use this when you need to read or compare flags across environments without redeploying, support multi-tenant routing, or combine server-side and client-side evaluations in one application.

To use multiple SDK keys in the example Kotlin Android application, follow these steps:

. Retrieve the SDK keys for the environments you’ll use.
. Initialize a separate SDK instance for each key.
. Decide how to route requests to instances (for example, by tenant, region, or environment selector).
. Perform register/fetch/stream setup on each instance as required by your SDK.
. Evaluate flags using the selected instance. Pass a consistent user/context object for accurate targeting.
. Tag logs or metrics by instance, and shut down instances you no longer need.

For more information about using multiple SDK keys, refer to https://docs.cloudbees.com/docs/cloudbees-platform/latest/feature-management/use-multiple-sdk-keys. 

## Documentation reference  

Refer to the CloudBees cloud-native platform documentation, link:https://docs.cloudbees.com/docs/cloudbees-platform/latest/install-sdk/[install the Feature management SDK] for more information.  
