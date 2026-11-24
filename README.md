# CoroutineDemo

Aplikasi Android (Kotlin + Jetpack Compose + MVVM) untuk tugas modul M17 – Kotlin Coroutines and Asynchronous Programming.

Aplikasi memuat daftar tugas, pengumuman, dan jumlah mata kuliah secara asinkron memakai `launch`, `async`, `withContext`, `coroutineScope`, dan `viewModelScope`.

## Fitur

| Tombol | Fungsi | Konsep |
|---|---|---|
| Load | Memuat tugas (2 dtk), pengumuman (1 dtk), dan jumlah mata kuliah (1,5 dtk) bersamaan | `async`, `withContext` |
| Load (fail) | Simulasi server gagal, tampil pesan error | `try/catch`, structured concurrency |
| Cancel | Menghentikan pemuatan | `Job.cancel()` |
| Retry | Muncul di samping pesan error, memanggil `loadData()` lagi | UI state |
| Sort A-Z / Z-A | Mengurutkan tugas berdasarkan judul (bolak-balik) | `withContext(Dispatchers.Default)` |
| Tap me | Penghitung untuk membuktikan layar tetap lancar | suspend tidak memblokir thread |
| Block UI | `Thread.sleep(3000)` di main thread (contoh buruk) | main thread terhambat |

## Struktur

```
com.example.coroutinedemo
├── data
│   ├── Task.kt
│   └── TaskRepository.kt
├── ui/main
│   ├── TasksUiState.kt
│   ├── TasksViewModel.kt
│   └── TasksScreen.kt
└── MainActivity.kt
```

## Screenshot

![Hasil Load](screenshoot/skrinsut.png)

## Jawaban Latihan (Bagian 9)

**1. Permintaan ketiga (`getCourseCount`).** Total waktu pemuatan masih sekitar 2 detik. Ketiga `async` berjalan bersamaan, sehingga total waktu sama dengan pekerjaan yang paling lama (2 detik), bukan penjumlahan 2 + 1 + 1,5 = 4,5 detik. Aplikasi menampilkan waktu pemuatan ("Loaded in ... ms") sebagai buktinya.

**2. Tombol Retry.** Tombol `Retry` ditampilkan di samping pesan error dan hanya muncul kalau `errorMessage != null`. Tombol ini memanggil `loadData()` lagi.

**3. `catch (e: Exception)`.** Setelah Load lalu Cancel, pesan yang muncul berubah dari "Loading cancelled" menjadi "Failed to load: ... was cancelled". Penyebabnya, saat coroutine dibatalkan, `await()` melempar `CancellationException`. Dengan `catch (e: IOException)` exception itu tidak tertangkap, tetapi `catch (e: Exception)` ikut menangkapnya. Blok catch lalu menimpa pesan dengan error palsu, padahal coroutine sebenarnya dibatalkan, bukan gagal. Solusinya: tangkap exception yang spesifik, atau lempar kembali `CancellationException` sebelum menangkap `Exception`.

**4. Di mana macetnya?**
- Dengan `Thread.sleep(2000L)` di dalam `withContext(Dispatchers.IO)`, layar tidak macet. Yang tertahan hanya thread IO, sedangkan main thread tetap bebas (tombol Tap me tetap bereaksi).
- Setelah `withContext` dihapus, `getTasks()` berjalan di main thread (dispatcher bawaan `viewModelScope` adalah Main), jadi `Thread.sleep` menahan main thread dan layar macet sekitar 2 detik.
- Pelajarannya: fungsi yang melakukan pekerjaan berat harus main-safe, yaitu memindahkan dirinya sendiri ke dispatcher yang tepat dengan `withContext`. Perhatikan juga bahwa `Thread.sleep` tidak bisa dibatalkan seperti `delay`.

**5. Pertanyaan konsep.**
- (a) `launch` menghasilkan `Job` dan tidak mengembalikan nilai, cocok untuk pekerjaan yang cukup dijalankan (menyimpan data, memperbarui state). `async` menghasilkan `Deferred<T>` yang nilainya diambil dengan `await()`, cocok untuk beberapa pekerjaan independen yang hasilnya dibutuhkan dan ingin dijalankan bersamaan.
- (b) `GlobalScope` tidak terikat lifecycle apa pun dan tidak pernah dibatalkan selama aplikasi hidup. Coroutine di dalamnya terus berjalan meskipun layar sudah ditutup, sehingga boros sumber daya, bisa bocor, dan keluar dari structured concurrency. Gunakan `viewModelScope` (atau scope lain yang mengikuti lifecycle).
- (c) Kalau `getTasks()` gagal, `IOException` membuat `coroutineScope` membatalkan child lain (sibling), termasuk `getAnnouncement()`, lalu error diteruskan ke `try/catch` di ViewModel. Pada aplikasi ini `getAnnouncement()` (1 detik) sebenarnya sudah selesai sebelum `getTasks()` gagal (2 detik), tetapi hasilnya tidak pernah dipakai karena state tidak diperbarui.

**6. Tantangan (Sort).** Tombol Sort memanggil `viewModel.sortTasks()`, yang memanggil `repository.sortByTitle()`. Pengurutan dijalankan dengan `withContext(Dispatchers.Default)`, lalu state diperbarui dengan hasilnya di Main.
