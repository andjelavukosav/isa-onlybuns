import { Component } from '@angular/core';
import { Post } from '../model/post.model';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../service/post.service';
import { UserDTO } from '../model/registered-user';
import { AuthService, UserService } from '../service';
import { Observable } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { icon, latLng, marker, tileLayer, Map } from 'leaflet';

import { Location } from '../model/location.model';

@Component({
  selector: 'app-post-details',
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css']
})
export class PostDetailsComponent {
  post: Post | undefined;
  currentUser: UserDTO | undefined ;
  whoamIResponse = {};
  isEditing: boolean = false; // Dodano za režim uređivanja
  postForm: FormGroup; // Forma za uređivanje posta
  
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
  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    private router: Router,
    private userService: UserService,
    private fb: FormBuilder // Injekcija FormBuildera za kreiranje forme
  ) {
    this.postForm = this.fb.group({
      description: [this.post?.description, [Validators.required, Validators.maxLength(255)]],
      locationLatitude: [this.post?.location.latitude],
      locationLongitude: [this.post?.location.longitude],
      image: [this.post?.imagePath] // Ako želite da preuzmete vrednost slike
    });
  }

  ngOnInit(): void {

    this.getCurrentUser('http://localhost:8080');

    const postId = this.route.snapshot.paramMap.get('id');
    if (postId) {
      this.postService.getPostById(+postId).subscribe(
        (post: Post) => {
          this.post = post;
          this.fillForm(post);
        },
        (error) =>{
          console.log('Error occured while loading post: ', error);
        }
      
      );
    }
  }

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


  onMapReady(map: Map): void { // Use Map here instead of LeafletMap
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

  fillForm(post: Post): void {
    if (this.postForm) {
      this.postForm.patchValue({
        description: post.description,
        locationLatitude: post.location.latitude,
        locationLongitude: post.location.longitude
      });
    }
  }

    getCurrentUser(path: any): void {
      this.userService.getMyInfo()
        .subscribe(res => {
          this.forgeResonseObj(this.whoamIResponse, res, path);
          this.currentUser = res; // Assign user info to currentUser
          console.log('current user ', this.currentUser)
        }, err => {
          this.forgeResonseObj(this.whoamIResponse, err, path);
        });
    }
  
    forgeResonseObj(obj: any, res: any, path: any) {
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

  deletePost(post: any): void {
    // Poziv ka servisu za brisanje posta
    if(post.user === undefined){
      console.log('User is undifined.');
      return;
    }
    this.postService.deletePost(post.id, post.user.id).subscribe({
      next: () => {
        alert('Post deleted successfully.');
        this.router.navigate(['/user-home']);
      },
      error: ()=> {
        alert('Post deleted successfully.');
        this.router.navigate(['/user-home']);      }
    }
      
    );
  } 

  onEdit(): void {
    this.isEditing = true; // Aktiviraj uređivanje
  }
  
  update(post: Post): void {
    if (this.currentUser == null) {
      alert('User is not logged in!');
      return;
    }
  
    if (this.postForm.valid && this.post) {
      this.userService.getUserById(this.currentUser.id).subscribe(
        (user) => {
          if (this.post !== undefined) {  // Proveravamo da li je ID validan
            const updatedPost : Post= {
              id: this.post.id,  // Očuvanje originalnog ID-a
              description: this.postForm.value.description,
              likeCount: this.post.likeCount,
              imagePath: this.post.imagePath,
              creationDateTime: new Date().toISOString().slice(0, 16), 
              location: this.post.location,
              user: user  // Dodajte kompletne podatke o korisniku
            };
  
            this.postService.updatePost(updatedPost, this.selectedImage).subscribe(
              (response) => {
                this.post = response;
                this.isEditing = false;
                alert('Post updated successfully!');
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
              (error) => {
                console.log('Error updating post: ', error);
                alert('Error updating post.');
              }
            );
          } else {
            alert('Post ID is invalid.');
          }
        },
        (error) => {
          console.log('Error fetching user data: ', error);
          alert('Error fetching user data.');
        }
      );
    } else {
      alert('Post form is not valid or post does not exist.');
    }
  }
  
  
}
