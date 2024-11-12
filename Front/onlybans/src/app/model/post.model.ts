import { Location } from "./location.model";
import { UserDTO } from "./registered-user"

export interface Post {
    id: number; // Jedinstveni identifikator posta
    userId: number; // ID korisnika koji je kreirao post
    description: string; // Opis objave
    imagePath: string; // Putanja do slike
    creationDateTime: string; // Vreme kreiranja posta u ISO 8601 formatu
    location: Location; // Lokacija objave
    username: string; // Korisničko ime autora objave
    usernameDisplay?: string;
    user?: UserDTO;
  }
  
 
  