import { Component, OnInit, OnDestroy } from '@angular/core';
import { UserService } from '../service';
import * as L from 'leaflet';
import { ActivatedRoute } from '@angular/router';
import { PostService } from '../service/post.service';
import { WebSocketService } from '../service/websocket.service';

@Component({
  selector: 'app-nearby-posts-map',
  templateUrl: './nearby-posts-map.component.html',
  styleUrls: ['./nearby-posts-map.component.css']
})
export class NearbyPostsMapComponent implements OnInit, OnDestroy {
  private map!: L.Map;
  userId: number | null = null;
  whoamIResponse = {};
  currentUserId: number | null = null;
  userLat: number | null = null;
  userLng: number | null = null;

  // Definisanje crvene ikone za korisnika
  private redIcon = L.icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34]
  });
  

  // Definisanje plave ikone za postove
  private blueIcon = L.icon({
    iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  private greenIcon = L.icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-green.png',
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34]
  });

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private postService: PostService,
    private webSocketService: WebSocketService // Dodato

  ) {}

  ngOnInit(): void {
    this.initMap();
    this.getCurrentUser('http://localhost:8080');
    this.userId = Number(this.route.snapshot.paramMap.get('userId'));

    this.userService.getUserLocation(this.userId).subscribe(location => {
      const { latitude, longitude } = location;
      this.userLat = latitude;
      this.userLng = longitude;

      this.map.setView([this.userLat, this.userLng], 15);

      // Dodavanje korisnikovog markera sa crvenom ikonom
      L.marker([this.userLat, this.userLng], { icon: this.redIcon })
        .addTo(this.map);
        //.bindPopup('Korisnikova lokacija')
        //.openPopup();

      // Sada kada imamo koordinate, učitavamo obližnje postove
      this.loadNearbyPosts();
    });
    this.loadAsylumsAndVeterinarians();

     // ✅ Pretplata na nove azile/veterinare
    this.webSocketService.messages$.subscribe(location => {
      const lat = location.address?.location?.latitude;
      const lng = location.address?.location?.longitude;
      const name = location.name;

      if (lat && lng) {
        L.marker([lat, lng], { icon: this.greenIcon })
          .addTo(this.map)
          .bindPopup(`<b>${name}</b><br>${location.address.street} ${location.address.number}, ${location.address.city}, ${location.address.country}`);
      } else {
        console.warn('Primljen entitet bez validne lokacije:', location);
      }
    });
  }

  private loadAsylumsAndVeterinarians(): void {
    this.userService.getAllLocations().subscribe(locations => {
      console.log("Podaci primljeni sa backenda:", locations);
  
      if (Array.isArray(locations)) {
        locations.forEach(location => {
          const lat = location.address?.location?.latitude;
          const lng = location.address?.location?.longitude;
          const name = location.name;
  
          if (lat && lng) {
            L.marker([lat, lng], { icon: this.greenIcon })
              .addTo(this.map)
              .bindPopup(`<b>${name}</b><br>${location.address.street} ${location.address.number}, ${location.address.city}, ${location.address.country}`);
          } else {
            console.warn('Nema validne lokacije za:', location);
          }
        });
      } else {
        console.error('Nevalidan format podataka:', locations);
      }
    });
  }
  

  loadNearbyPosts(): void {
    if (this.userLat !== null && this.userLng !== null) {
      this.postService.getNearbyPosts(this.userLat, this.userLng).subscribe(response => {
        const posts = response.results; // Pristupamo 'results' iz odgovora

        if (Array.isArray(posts)) {
          posts.forEach(post => {
            // Provera da li post sadrži validnu lokaciju
            if (post?.location?.latitude && post?.location?.longitude) {
              L.marker([post.location.latitude, post.location.longitude], { icon: this.blueIcon })
                .addTo(this.map);
            } else {
              console.warn('Post nema validnu lokaciju:', post);
            }
          });
        } else {
          console.error('Nema validnog niza postova:', posts);
        }
      });
    }
  }



  private initMap(): void {
    if (!this.map) {
      this.map = L.map('map').setView([44.7866, 20.4489], 13); // Default Beograd

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
      }).addTo(this.map);
    }
  }

  getCurrentUser(path: any): void {
    this.userService.getMyInfo()
      .subscribe(res => {
        this.forgeResonseObj(this.whoamIResponse, res, path);
        this.currentUserId = res.id;
      }, err => {
        this.forgeResonseObj(this.whoamIResponse, err, path);
      });
  }

  forgeResonseObj(obj: any, res: any, path: any): void {
    obj['path'] = path;
    obj['method'] = 'GET';
    if (res.ok === false) {
      obj['status'] = res.status;
      try {
        obj['body'] = JSON.stringify(JSON.parse(res._body), null, 2);
      } catch (err) {
        console.log(res);
        obj['body'] = res.error.message;
      }
    } else {
      obj['status'] = 200;
      obj['body'] = JSON.stringify(res, null, 2);
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }
}
