import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { PostService } from '../service/post.service';
import { Post } from '../model/post.model';
import { Location } from '../model/location.model';
import { icon, latLng, marker, tileLayer, Map } from 'leaflet';
import { AuthService, UserService } from '../service';


@Component({
  selector: 'app-create-post',
  templateUrl: './create-post.component.html',
  styleUrls: ['./create-post.component.css']
})
export class CreatePostComponent {
  @Input() post: Post | null = null;
  selectedImage: File | null = null;
  previewUrl: string | null = null;
  
  // Mapa koordinata
  map: Map | undefined;
  marker: any;  // Marker na mapi
  location: Location = { latitude: 0, longitude: 0 }; // Početne koordinate

  // Opcije za mapu (centar i zoom nivo)
  options = {
    layers: [
      tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      })
    ],
    zoom: 13,
    center: latLng(51.505, -0.09) // Početni centar mape
  };

  constructor(private postService: PostService, private authService: AuthService) {}

  postForm = new FormGroup({
    description: new FormControl('', [Validators.required, Validators.maxLength(255)]),
    createdAt: new FormControl(new Date().toISOString().slice(0, 16), Validators.required),
    locationLatitude: new FormControl(''), 
    locationLongitude: new FormControl(''), 
  });
  
  ngOnChanges(): void {
    if (this.post) {
      this.postForm.patchValue({
        description: this.post.description,
        createdAt: this.post.creationDateTime,
      });

      // Postavljanje markera sa postojećim koordinatama
      this.location = this.post.location;
      this.setMarker(this.location.latitude, this.location.longitude);
    }
  }


  onMapReady(map: Map): void { // Use `Map` here instead of `LeafletMap`
    this.map = map;
    this.map.on('click', (event: L.LeafletMouseEvent) => {
      const latLng = event.latlng;
      this.setMarker(latLng.lat, latLng.lng);
      this.location = { latitude: latLng.lat, longitude: latLng.lng };
      
      this.postForm.patchValue({
        locationLatitude: this.location.latitude.toString(),
        locationLongitude: this.location.longitude.toString(),
      });
    });
  }

  setMarker(lat: number, lng: number) {
    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      this.marker = marker([lat, lng], { icon: icon({ iconUrl: 'assets/marker-icon.png' }) }).addTo(this.map!);
    }
  }

  onImageSelected(event: any): void {
    const file = event.target.files[0];
    if (file && file.type.startsWith('image/')) {
      this.selectedImage = file;
  
      // Kreiranje preview slike
      const reader = new FileReader();
      reader.onload = () => {
        this.previewUrl = reader.result as string;
      };
      reader.readAsDataURL(file);
    } else {
      alert('Molimo odaberite validnu sliku.');
    }
  }

  createPost(): void {
    console.log('Metoda createPost() je pozvana');

    if (this.postForm.valid && this.selectedImage) {
      const formValues = this.postForm.value;
  
  
      const newPost: Post = {
        id: 0, // Backend će generisati ID
        description: formValues.description!,
        imagePath: '', // Backend će popuniti putanju slike
        creationDateTime: formValues.createdAt!,
        location: this.location
      };

      // Pozovi servis i proslijedi `newPost` i `selectedImage`
      this.postService.addPost(newPost, this.selectedImage).subscribe({
        next: (createdPost) => {
          console.log('Post successfully created:', createdPost);

          // Resetovanje forme i čišćenje slike
          this.postForm.reset(); // Resetuje formu
          this.selectedImage = null; // Briše selektovanu sliku
          this.previewUrl = null; // Uklanja preview slike
          this.marker = null; // Uklanja marker sa mape
          if (this.map) {
            this.map.eachLayer((layer) => {
              if ((layer as any).options.icon) {
                this.map!.removeLayer(layer); // Uklanja marker sa mape
              }
            });
          }
        },
        error: (err) => {
          console.error('Error creating post:', err);
          alert('Greška prilikom kreiranja posta. Pokušajte ponovo.');
        },
      });
    } else {
      alert('Molimo popunite sve obavezne podatke i dodajte sliku.');
    }
  }
  
  
}
