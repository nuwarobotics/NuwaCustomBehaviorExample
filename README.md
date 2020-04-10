# NuwaCustomBehaviorExample
Nuwa CustomBehavior allow developer receive notify from NLP result.
 * Target SDK : `2.0.0.06`

# `Nuwa Website`
* NuwaRobotics Website (https://www.nuwarobotics.com/)
* NuwaRobotics Developer Website (https://dss.nuwarobotics.com/)


NOTICE : Please get NuwaSDK from Nuwarobotics Developer Website

# `Support Robot Product`
Robot Generation 2
* Kebbi Air : Taiwan、China、Japan

# `CustomBehavior SDK`
 * Target SDK : `2.0.0.06`
 * [Nuwa Trainkit Website](https://trainkit.nuwarobotics.com) (Only Allow Business Partner NOW)

# `Custom Behavior`

System behavior allows developer to customize response behavior of a NLP result.
Developer need to setup Chatbot Q&A from NUWA Trainkit website which allow developer setup Custom Intention for a sentence.
Following sample code will present how to register receive this CustomIntentation notify and implement customize response.


## class BaseBehaviorService
Developer should implement a class to determine how to react when receiving a customized NLP response from NUWA Trainkit. 
This could be achieved by extending from class BaseBehaviorService.
BaseBehaviorService declared three important functions which onInitialize(), createCustomBehavior() and notifyBehaviorFinished(). 
There are two functions need to be implemented which onInitialize() and createCustomBehavior(). 

* onInitialize()
	+ When the extending class has been started, developer could initialize resource here.


* createCustomBehavior()
	+ After that, the other function createCustomBehavior should create a CustomBehavior object. 
	CustomBehavior is a interface which define custom behavior once receiving customized NLP result. 
	It declared three callback functions for developer to implement customized behavior.


* notifyBehaviorFinished()
	+ To notify robot behavior system that the process has been completed.


## class ISystemBehaviorManager
This class has been encapsulated in BaseBehavior. Developer can get this instance which is created by BaseBehavior.

* register(String pkgName, CustomBehavior action)
	+ Register a CustomBehavior object to robot behavior system by app package name.


* unregister(String pkgName, CustomBehavior action)
	+ Unregister a CustomBehavior object to robot behavior system by app package name.


* setWelcomeSentence(String[] sentences)
	+ To set welcome sentence when robot detected someone. 


* resetWelcomeSentence()
	+ To reset welcome sentence.

* completeCustomBehavior()
	+ To notify robot behavior system that the process has been completed.


## class CustomBehavior
The Object which let developer define how to to deal with customized NLP response.

* prepare(String parameter)
	+ Once NLP response which is defined form TrainKit has been reached, the robot behavior system will start to prepare resource for this session. 
	The system notifies 3rd party APP and it could do something while preparing.


* process(String parameter)
	+ Developer could implement his logic here to deal with NLP response which has been defined from TrainKit.
	As robot behavior system is ready, this function call will be invoked.
	Note that the process might be an asynchronous task so that the developer must notify robot behavior system that the process when will be completed by notifyBehaviorFinished().


* finish(String parameter)
	+ When this customized behavior has been completed, robot behavior system will clean related resources and finish this session.
	At the same time, it notifies the 3rd party app this session has been finished by this function call.


## Sample code
```java
public class CustomBehaviorImpl extends BaseBehaviorService {

    @Override
    public void onInitialize() {
        handler = new Handler(Looper.getMainLooper());
        try {
        	// TODO initialize 
            mSystemBehaviorManager.setWelcomeSentence(new String[]{"你好， %s.這是一個歡迎詞的測試!", "%s, 挖底家", "%s, 有什麼可以為您服務的嗎？"});
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CustomBehavior createCustomBehavior() {
        return new CustomBehavior.Stub() {
            @Override
            public void prepare(final String parameter) {
				// TODO write your preparing work
            }

            @Override
            public void process(final String parameter) {
				// TODO the actual process logic

                // TODO simulate asynchronous task while process complete
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            notifyBehaviorFinished();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, 5000);
            }

            @Override
            public void finish(final String parameter) {
            	// TODO the whole session has been finished. 
            }
        };
    }

}
```
