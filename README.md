Android Data Client SDK
=======================

Features
--------

The Pivotal Mobile Services Suite Data Client SDK is a light-weight library that will help your application:

 1. Authenticate a user against an OpenID Connect server

 2. Store and retrieve key value data using the users access token
 
 
Device Requirements
-------------------

The Push SDK requires API level 10 or greater.


Getting Started
---------------

 1. Link the library to your project.  This project has not yet been published to any Maven repositories, but once it has 
    then you can add the following line to the `dependencies` section of your `build.gradle` file:
   
    ```groovy
    compile 'io.pivotal.android:data:1.0.0'
    ```
   
    Note that the version name may be different.
   
   	Even if you don't have access to a Maven repository with this library, you could still link to the source of this module,
   	or simply obtain the compiled AAR files.  Please contact the Pivotal Mobile Services Suite team for help.

 2. Before you can persist data using the data store, the user must authorize your application against an OpenID Connect 
    server. To begin this process within your application, you will need to configure the `DataStore` object with some parameters. 
    Then you can make a call to the `obtainAuthorization` method, passing the current activity as an argument.

    ```java
    final DataStoreParameters parameters = new DataStoreParameters(
        CLIENT_ID, CLIENT_SECRET, AUTHORIZATION_URL, TOKEN_URL, REDIRECT_URL, DATA_SERVICES_URL
    );

    final DataStore datastore = DataStore.getInstance();
    datastore.setParameters(this, parameters);
    datastore.obtainAuthorization(this);
    ```

    The `CLIENT_ID`, `CLIENT_SECRET`, `AUTHORIZATION_URL`, `TOKEN_URL` and `REDIRECT_URL` are all obtained from the identity
    provider. The `DATA_SERVICES_URL` parameter is the base url of your data server.
   	
 3. The `obtainAuthorization` method will kick off an intent to an external browser in order to perform authorization. You need 
    to create an authorization activity that extends the `BaseAuthorizationActivity` class. This activity is going to handle all 
    the callbacks from the browser. Your implementation might look something like the following in a very simple case.
    
    ```java
    public class AuthorizationActivity extends BaseAuthorizationActivity {
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_authorization);
        }
    
        @Override
        public void onAuthorizationComplete() {
            // handle success case
            finish()
        }
    
        @Override
        public void onAuthorizationDenied() {
            // handle denied case
            finish()
        }
    
        @Override
        public void onAuthorizationFailed(String reason) {
            // handle failure case
            finish()
        }
    }
    ```
 
 4. You will need to add your authorization activity to your application's `AndroidManifest.xml`. The `activity` tag 
    needs to contain the following `intent-filter` that captures the redirect URL sent by the server. Make 
    sure you update the `scheme`, `host` and `pathPrefix` with those corresponding to your application.

    ```xml
    <activity android:name=".AuthorizationActivity">
        <intent-filter>
           <action android:name="android.intent.action.VIEW" />
           <category android:name="android.intent.category.DEFAULT" />
           <category android:name="android.intent.category.BROWSABLE" />

           <data android:scheme="[YOUR.REDIRECT_URL.SCHEME]" />
           <data android:host="[YOUR.REDIRECT_URL.HOST]" />
           <data android:pathPrefix="[YOUR.REDIRECT_URL.PATH]" />
        </intent-filter>
    </activity>
    ```
    
 5. After your application has been succesffully authorized. You can save and fetch objects as follows:
 
    ```java
    public void fetchObject() {
        final DataObject object = new DataObject("objects");
        object.setObjectId("my-object");

        object.fetch(datastore.getClient(this), new DataListener() {

            @Override
            public void onSuccess(final DataObject object) {
                showToast("Object retrieved successfully");
            }

            @Override
            public void onUnauthorized(DataObject object) {
                showToast("Authorization error fetching object");
            }

            @Override
            public void onFailure(DataObject object, String reason) {
                showToast(reason);
            }
        });
    }
    ```

    ```java
    public void saveObject() {
        final DataObject object = new DataObject("objects");
        object.setObjectId("my-object");
        object.put("key1", "value1");
        object.put("key2", "value2");

        object.save(datastore.getClient(this), new DataListener() {
            @Override
            public void onSuccess(DataObject object) {
                showToast("Object saved successfully");
            }

            @Override
            public void onUnauthorized(DataObject object) {
                showToast("Authorization error saving object");
            }

            @Override
            public void onFailure(DataObject object, String reason) {
                showToast(reason);
            }
        });
    }
    ```