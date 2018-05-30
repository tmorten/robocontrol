#include <Servo.h>

// Define pins
int en_A = 4;   // Right motor enable (PWM)
int in1 = 2;    // Control pin
int in2 = 3;

int en_B = 7;   // Left motor enable (PWM)
int in3 = 5;
int in4 = 6;

// Motor speed
#define initialSpeed 100

int sensor[5] = {1, 1, 1, 1, 1};

// PID
float Kp = 2, Ki = 0.5, Kd = 5;
float error = 0, P = 0, I = 0, D = 0, PID_val = 0;
float prev_error = 0, prev_I = 0;

char data;

// Sonar
#define trigPin 8
#define echoPin 9

long duration;
int distance;

// Servo
int servoMotorPin = 10;
Servo servoMotor;

int cycle = 0;
bool up = true;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  pinMode(en_A, OUTPUT);
  pinMode(in1, OUTPUT);
  pinMode(in2, OUTPUT);

  pinMode(en_B, OUTPUT);
  pinMode(in3, OUTPUT);
  pinMode(in4, OUTPUT);

  pinMode(A0, INPUT);
  pinMode(A1, INPUT);
  pinMode(A2, INPUT);
  pinMode(A3, INPUT);
  pinMode(A4, INPUT);

  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);

  servoMotor.attach(servoMotorPin);
}

void loop() {

  servoMotor.write(cycle);
    delay(200);
    digitalWrite(trigPin, LOW);
    delayMicroseconds(2);
    digitalWrite(trigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(trigPin, LOW);
    duration = pulseIn(echoPin, HIGH);
    distance = duration * 0.034 / 2;
    if (distance < 50) {
      delay(200);
      Serial.print(distance);
      Serial.print('-');
      Serial.println(cycle);
    }


  //Serial.print("Distance: ");


  //readSensorValues();
  //calculatePid();
  //motorControl();
  analogWrite(en_A, initialSpeed);
  analogWrite(en_B, initialSpeed - 5);

  if (Serial.available() > 0) {
    data = Serial.read();
    //Serial.println(data);
    if (data == 'f') {
      //Serial.println("FORWARD");
      Forward();
    } else if (data == 'b') {
      //Serial.println("BACKWARD");
      Backward();
    } else if (data == 'l') {
      //Serial.println("LEFT");
      Right();
    } else if (data == 'r') {
      //Serial.println("RIGHT");
      Left();
    } else if (data == 's') {
      Stop();
    }
  }

  if (cycle < 120 && up == true) {
    cycle+=30;
  } else if (cycle == 120 && up == true) {
    cycle -= 30;
    up = false;
  } else if (cycle > 0 && up == false) {
    cycle -= 30;
  } else if (cycle == 0 && up == false) {
    cycle += 30;
    up = true;
  }
}

void Forward() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, HIGH);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
}

void Backward() {
  digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW);
  digitalWrite(in3, LOW);
  digitalWrite(in4, HIGH);
}

void Left() {
  digitalWrite(in1, HIGH);
  digitalWrite(in2, LOW);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
}

void Right() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, HIGH);
  digitalWrite(in3, LOW);
  digitalWrite(in4, HIGH);
}

void Stop() {
  digitalWrite(in1, LOW);
  digitalWrite(in2, LOW);
  digitalWrite(in3, LOW);
  digitalWrite(in4, LOW);
  analogWrite(en_A, LOW);
  analogWrite(en_B, LOW);
}

void readSensorValues() {
  // Read sensors
  sensor[0] = digitalRead(A0);
  sensor[1] = digitalRead(A1);
  sensor[2] = digitalRead(A2);
  sensor[3] = digitalRead(A3);
  sensor[4] = digitalRead(A4);

  Serial.print(sensor[0]);
  Serial.print(" | ");
  Serial.print(sensor[1]);
  Serial.print(" | ");
  Serial.print(sensor[2]);
  Serial.print(" | ");
  Serial.print(sensor[3]);
  Serial.print(" | ");
  Serial.print(sensor[4]);
  Serial.println();

  //  1--1--1--1--0
  if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 1 && sensor[3] == 1 && sensor[4] == 0 )
    error = 4;
  //  1--1--1--0--0
  else if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 1 && sensor[3] == 0 && sensor[4] == 0 )
    error = 3;
  //  1--1--1--0--1
  else if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 1 && sensor[3] == 0 && sensor[4] == 1 )
    error = 2;
  //  1--1--0--0--1
  else if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 0 && sensor[3] == 0 && sensor[4] == 1 )
    error = 1;
  //  1--1--0--1--1
  else if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 0 && sensor[3] == 1 && sensor[4] == 1 )
    error = 0;
  //  1--0--0--1--1
  else if ( sensor[0] == 1 && sensor[1] == 0 && sensor[2] == 0 && sensor[3] == 1 && sensor[4] == 1 )
    error = -1;
  //  1--0--1--1--1
  else if ( sensor[0] == 1 && sensor[1] == 0 && sensor[2] == 1 && sensor[3] == 1 && sensor[4] == 1 )
    error = -2;
  //  0--0--1--1--1
  else if ( sensor[0] == 0 && sensor[1] == 0 && sensor[2] == 1 && sensor[3] == 1 && sensor[4] == 1 )
    error = -3;
  //  0--1--1--1--1
  else if ( sensor[0] == 0 && sensor[1] == 1 && sensor[2] == 1 && sensor[3] == 1 && sensor[4] == 1 )
    error = -4;
  //  1--1--1--1--1
  else if ( sensor[0] == 1 && sensor[1] == 1 && sensor[2] == 1 && sensor[3] == 1 && sensor[4] == 1 )
    if ( error == -4 ) error = -5;
    else error = 5;
}

void calculatePid() {
  P = error;
  I = I + prev_I;
  D = error - prev_error;

  PID_val = (Kp * P) + (Ki * I) + (Kd * D);

  prev_I = I;
  prev_error = error;
}

void motorControl() {
  // calculate motor speed
  int leftMotorSpeed = initialSpeed + PID_val;
  int rightMotorSpeed = initialSpeed - PID_val;

  // motor speed should not have more than the max PWM value: 0 - 255
  constrain(leftMotorSpeed, 0, 255);
  constrain(rightMotorSpeed, 0, 255);

  analogWrite(en_A, rightMotorSpeed);
  analogWrite(en_B, leftMotorSpeed);

  digitalWrite(in1, LOW);
  digitalWrite(in2, HIGH);
  digitalWrite(in3, HIGH);
  digitalWrite(in4, LOW);
}
