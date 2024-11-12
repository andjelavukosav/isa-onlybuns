import { Location } from "./location.model";
import { UserDTO } from "./registered-user"

export interface Post {
    id: number; // Jedinstveni identifikator posta
    description: string; // Opis objave
    imagePath: string; // Putanja do slike
    creationDateTime: string; // Vreme kreiranja posta u ISO 8601 formatu
    location: Location; // Lokacija objave
    usernameDisplay?: string;
    user?: UserDTO;
    //likeCount?: number;
  }


