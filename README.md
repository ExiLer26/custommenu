# Custom Menu Mod - Minecraft Forge 1.20.1

Bu proje, Minecraft 1.20.1 için özel menü mod'udur.

## Özellikler
- Özel GUI menüleri oluşturma
- Komutlar ile menü yönetimi
- Tuş bağlama ile menü açma
- Menü itemlerine komut atama

## Komutlar
- `/menu create <isim> [slot sayısı] [başlık]` - Yeni menü oluştur
- `/menu open <isim>` - Menü aç
- `/menu add <menu> <slot> <item> <isim> <komut>` - Menüye item ekle
- `/menu remove <menu> <slot>` - Menüden item çıkar
- `/menu delete <isim>` - Menü sil
- `/menu list` - Menüleri listele
- `/menu reload` - config yeniden yükler.

## Tuş Bağlama
- M tuşu ile varsayılan menüyü açabilirsiniz
- K tuşu ile menu düzenleyici açabilirsiniz

## Build
```bash
./gradlew build
```

## Teknik Detaylar
- Minecraft 1.20.1
- Fabric Loader 0.15.11+
- Fabric API 0.92.0+1.20.1
- Java 17+
