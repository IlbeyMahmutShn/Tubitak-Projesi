#include <Wire.h>
#include <MPU6050.h>
#include <SoftwareSerial.h>
#include <TinyGPSPlus.h>
#include <SPI.h>
#include <SD.h>
#include <math.h>

// Modül tanımlamaları
MPU6050 mpu;
SoftwareSerial gpsSerial(3, 4);
TinyGPSPlus gps;

const int irPin = A0;
const int chipSelect = 6;
const char* filename = "veri.csv";

const int ax_offset = 1285;
const int ay_offset = -324;
const int az_offset = 3730;
const int gx_offset = -613;
const int gy_offset = 5;
const int gz_offset = -87;
const int ledPin = 7;
const int ledPin1 = 8;
const int ledPin2 = 9;
const int ledPin3 = 10;

void setup() {
  Serial.begin(9600);
  Wire.begin();
  gpsSerial.begin(9600);
  pinMode(ledPin, OUTPUT);
  pinMode(ledPin1, OUTPUT);
  pinMode(ledPin2, OUTPUT);
  pinMode(ledPin3, OUTPUT);


  mpu.initialize();
  if (!mpu.testConnection()) {
    Serial.println("MPU bağlantı hatası!");
    while (1){
      digitalWrite(ledPin1, HIGH);
      delay(500);
      digitalWrite(ledPin1, LOW);
      delay(500);
    }
  }

  if (!SD.begin(chipSelect)) {
    Serial.println("SD kart hatası!");
    while (1){
      digitalWrite(ledPin2, HIGH);
      delay(500);
      digitalWrite(ledPin2, LOW);
      delay(500);
    }
  }

  if (SD.exists(filename)) SD.remove(filename);
  File file = SD.open(filename, FILE_WRITE);
  if (file) {
    file.println("Zaman,AccX,AccY,AccZ,GyroX,GyroY,GyroZ,IRcm,Lat,Lon,Hiz");
    file.close();
  }
  Serial.println("Sistem başlatıldı.");
  digitalWrite(ledPin3, HIGH);

}

void loop() {
  long sum_ax = 0, sum_ay = 0, sum_az = 0;
  long sum_gx = 0, sum_gy = 0, sum_gz = 0;
  float sum_ir = 0;
  double sum_lat = 0, sum_lon = 0, sum_hiz = 0;

  int samples = 5;

  for (int i = 0; i < samples; i++) {
    int16_t ax, ay, az, gx, gy, gz;
    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);

    ax -= ax_offset;
    ay -= ay_offset;
    az -= az_offset;
    gx -= gx_offset;
    gy -= gy_offset;
    gz -= gz_offset;

    int ir = analogRead(irPin);
    float volt = ir * 5.0 / 1023.0;
    float irCm = 27.86 / pow(volt, 1.15);

    while (gpsSerial.available()) {
      gps.encode(gpsSerial.read());
    }

    double lat = gps.location.isValid() ? gps.location.lat() : 0.0;
    double lon = gps.location.isValid() ? gps.location.lng() : 0.0;
    double hiz = gps.speed.isValid() ? gps.speed.kmph() : 0.0;

    if (!gps.location.isValid()) {
  digitalWrite(ledPin, HIGH);  // Uydudan veri alınamıyor, LED YANAR
} else {
  digitalWrite(ledPin, LOW);   // Uydudan veri alınıyor, LED SÖNER
}

    sum_ax += ax;
    sum_ay += ay;
    sum_az += az;
    sum_gx += gx;
    sum_gy += gy;
    sum_gz += gz;
    sum_ir += irCm;
    sum_lat += lat;
    sum_lon += lon;
    sum_hiz += hiz;

    delay(40); //200 ms toplam
  }

  long avg_ax = sum_ax / samples;
  long avg_ay = sum_ay / samples;
  long avg_az = sum_az / samples;
  long avg_gx = sum_gx / samples;
  long avg_gy = sum_gy / samples;
  long avg_gz = sum_gz / samples;
  float avg_ir = sum_ir / samples;
  double avg_lat = sum_lat / samples;
  double avg_lon = sum_lon / samples;
  double avg_hiz = sum_hiz / samples;

  String zaman = "00:00:00";
  if (gps.time.isValid()) {
    zaman = String(gps.time.hour()+3) + ":" +
            (gps.time.minute() < 10 ? "0" : "") + String(gps.time.minute()) + ":" +
            (gps.time.second() < 10 ? "0" : "") + String(gps.time.second());
  }

  // Seri monitör
  Serial.print(zaman); Serial.print(",");
  Serial.print(avg_ax); Serial.print(",");
  Serial.print(avg_ay); Serial.print(",");
  Serial.print(avg_az); Serial.print(",");
  Serial.print(avg_gx); Serial.print(",");
  Serial.print(avg_gy); Serial.print(",");
  Serial.print(avg_gz); Serial.print(",");
  Serial.print(avg_ir); Serial.print(",");
  Serial.print(avg_lat, 6); Serial.print(",");
  Serial.print(avg_lon, 6); Serial.print(",");
  Serial.println(avg_hiz, 2);

  // SD kart
  File file = SD.open(filename, FILE_WRITE);
  if (file) {
    file.print(zaman); file.print(",");
    file.print(avg_ax); file.print(",");
    file.print(avg_ay); file.print(",");
    file.print(avg_az); file.print(",");
    file.print(avg_gx); file.print(",");
    file.print(avg_gy); file.print(",");
    file.print(avg_gz); file.print(",");
    file.print(avg_ir); file.print(",");
    file.print(avg_lat, 6); file.print(",");
    file.print(avg_lon, 6); file.print(",");
    file.println(avg_hiz, 2);
    file.close();
  } else {
    Serial.println("Dosya açılamadı! (Loop)");
    digitalWrite(ledPin3, LOW);
    digitalWrite(ledPin2, HIGH);
      delay(500);
      digitalWrite(ledPin2, LOW);
      delay(500);
  }
}
