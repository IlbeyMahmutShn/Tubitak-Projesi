import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix
import matplotlib.pyplot as plt
import seaborn as sns
import json
import firebase_admin
from firebase_admin import credentials, db
from geopy.distance import geodesic

def hiz_filtresi(df, max_hiz_kmh=100):
    temiz_indexler = [0]  # ilk nokta her zaman kabul
    
    for i in range(1, len(df)):
        onceki = (df.loc[i-1, "Lat"], df.loc[i-1, "Lon"])
        suanki = (df.loc[i, "Lat"], df.loc[i, "Lon"])
        
        mesafe_m = geodesic(onceki, suanki).meters
        zaman_s = (pd.to_datetime(df.loc[i, "Zaman"]) - pd.to_datetime(df.loc[i-1, "Zaman"])).total_seconds()
        
        if zaman_s == 0:
            continue
        
        hiz_kmh = (mesafe_m / zaman_s) * 3.6
        
        if hiz_kmh <= max_hiz_kmh:
            temiz_indexler.append(i)
        else:
            print(f"Atılan nokta index={i}, hız={hiz_kmh:.2f} km/h")
    
    return df.loc[temiz_indexler].reset_index(drop=True)

def model_egit_ve_tahmin(egitim_dosyasi, test_dosyasi, json_kayit_dosyasi):
    # Eğitim verisini oku
    df_train = pd.read_csv(egitim_dosyasi, encoding="ISO-8859-9")
    
    # Zaman sütununu kaldır, label hedef değişken
    X_train = df_train.drop(columns=["label", "Zaman"], errors='ignore')
    y_train = df_train["label"]
    
    # Modeli eğit
    model = RandomForestClassifier(n_estimators=100, random_state=42)
    model.fit(X_train, y_train)
    
    # Test verisini oku
    df_test = pd.read_csv(test_dosyasi, encoding="ISO-8859-9")
    
    # Hız filtresi ile GPS verilerini temizle
    df_test = hiz_filtresi(df_test)
    
    X_test = df_test.drop(columns=["label", "Zaman"], errors='ignore')
    
    # Tahmin yap
    y_pred = model.predict(X_test)
    df_test["Tahmin"] = y_pred

    # Performans metrikleri
    if "label" in df_test.columns:
        y_true = df_test["label"]
        print("Model Başarım Raporu:")
        print(classification_report(y_true, y_pred))
        print("Confusion Matrix:")
        print(confusion_matrix(y_true, y_pred))
        print(f"Model doğruluk oranı: {model.score(X_test, y_true):.2%}")
    
    # Grafikleri çiz
    plt.figure(figsize=(6, 4))
    sns.countplot(x="Tahmin", data=df_test)
    plt.title("Tahmin Dağılımı")
    plt.xlabel("Tahmin (0: Düzgün Yol, 1: Bozuk Yol)")
    plt.ylabel("Örnek Sayısı")
    plt.tight_layout()
    plt.show()

    sensor_cols = ["AccX", "AccY", "AccZ", "GyroX", "GyroY", "GyroZ", "IRcm"]
    for sensor in sensor_cols:
        if sensor in df_test.columns:
            plt.figure(figsize=(10, 4))
            sns.lineplot(data=df_test, x=df_test.index, y=sensor, hue="Tahmin", palette="Set1")
            plt.title(f"{sensor} Sensör Verileri")
            plt.xlabel("İndeks")
            plt.ylabel(sensor)
            plt.legend(title="Tahmin")
            plt.tight_layout()
            plt.show()
    
    # CSV ve JSON olarak kaydet
    df_test.to_csv("test_sonuc.csv", index=False)
    print("Sonuçlar 'test_sonuc.csv' olarak kaydedildi.")

    json_veri = df_test[["Zaman", "Lat", "Lon", "Tahmin"]].copy()
    veriler = json_veri.to_dict(orient="records")
    
    with open(json_kayit_dosyasi, "w", encoding="utf-8") as f:
        json.dump(veriler, f, indent=4, ensure_ascii=False)
    
    print(f"Sonuçlar '{json_kayit_dosyasi}' olarak JSON biçiminde kaydedildi.")
    return veriler

def firebase_baglantisi_baslat_ve_veri_yukle(json_veriler, firebase_key_dosyasi):
    # Firebase'i sadece bir kez initialize et
    if not firebase_admin._apps:
        cred = credentials.Certificate(firebase_key_dosyasi)
        firebase_admin.initialize_app(cred, {
            'databaseURL': 'https://carpediemfit-7afeb-default-rtdb.firebaseio.com/'
        })
    
    ref = db.reference("tahminler")

    # JSON listesini dict'e dönüştür (Zaman -> veri)  
    veri_dict = {}
    for veri in json_veriler:
        zaman = veri.get("Zaman", None)
        if zaman:
            veri_dict[zaman] = veri

    # Toplu gönderim (tek istek)
    ref.set(veri_dict)
    print("Veriler toplu halde Firebase'e yüklendi.")

if __name__ == "__main__":
    # Dosya yolları
    egitim_dosyasi = "C:/Users/Msahi/Downloads/veri.csv"
    test_dosyasi = "C:/Users/Msahi/Downloads/test.csv"
    json_kayit_dosyasi = "C:/Users/Msahi/OneDrive/Masaüstü/Tübitak Porjesi/Json/test_sonuc.json"
    firebase_key_dosyasi = "C:/Users/Msahi/OneDrive/Masaüstü/Tübitak Porjesi/Json/firebase_key.json"

    # Model eğit ve tahmin yap, JSON veriyi hazırla
    json_veriler = model_egit_ve_tahmin(egitim_dosyasi, test_dosyasi, json_kayit_dosyasi)

    # Firebase'e veriyi tek seferde yükle
    firebase_baglantisi_baslat_ve_veri_yukle(json_veriler, firebase_key_dosyasi)
