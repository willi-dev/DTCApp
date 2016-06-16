# DTCApp
dtcapp (server):
Distributed twitter crawler adalah aplikasi yang saya buat untuk riset skripsi saya.    
Menggunakan bahasa pemrograman java, dibuat dengan menggunakan IDE Eclipse Helios version serta library Hazelcast.     
Aplikasi ini terdiri dari dtcapp (server), dtcapp client, dan antarmuka berbasis web PHP 
yang menerima input berupa kata kunci pencarian tweet yang ingin dicari dan kemudian setelah 
selesai akan menampilkan hasil pencariannya.    
repository ini merupakan source code untuk dtcapp (server). 
Pada dtcapp (server) (yang dipasang di beberapa mesin) ada 1 node yang berperan sebagai koordinator, 
jika koordinator mati, maka node yang lain akan melakukan pemilihan koordinator baru menggunakan algoritma ring.
