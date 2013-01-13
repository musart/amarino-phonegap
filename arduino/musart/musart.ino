#include <MeetAndroid.h>

MeetAndroid meetAndroid;
const int onboardLed = 13;
const int buttonPin = 2;

int buttonState = 0;         // variable for reading the pushbutton status
int buttonEventRegistered = 0;
int buttonPrevState = 0;

void setup()  
{
  // use the baud rate your bluetooth module is configured to 
  // not all baud rates are working well, i.e. ATMEGA168 works best with 57600
  Serial.begin(57600); 
  
  // register callback functions, which will be called when an associated event occurs.
  // - the first parameter is the name of your function (see below)
  // - match the second parameter ('A', 'B', 'a', etc...) with the flag on your Android application
  meetAndroid.registerFunction(controlLed, 'L');
  meetAndroid.registerFunction(buttonEvent, 'B');

  pinMode(onboardLed, OUTPUT);
  digitalWrite(onboardLed, HIGH);

}

void loop()
{
  meetAndroid.receive(); // you need to keep this in your loop() to receive events

  if(buttonEventRegistered == 1)
  {
    buttonState = digitalRead(buttonPin);
    if(buttonState == HIGH) {
      if(buttonPrevState != buttonState) {
        meetAndroid.send("B:pressed");
      }
    } else {
      if(buttonPrevState != buttonState) {
        meetAndroid.send("B:released");
      }
    }
    buttonPrevState = buttonState;
  }
  delay(300);
}

void buttonEvent(byte flag, byte numOfValues)
{
  buttonEventRegistered = 1;
}

/*
 * This method is called constantly.
 * note: flag is in this case 'A' and numOfValues is 0 (since test event doesn't send any data)
 */
void controlLed(byte flag, byte numOfValues)
{
  flushLed(300);
  flushLed(300);
}

void flushLed(int time)
{
  digitalWrite(onboardLed, LOW);
  delay(time);
  digitalWrite(onboardLed, HIGH);
  delay(time);
}

