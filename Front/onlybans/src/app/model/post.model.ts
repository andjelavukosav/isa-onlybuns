import { Location } from "./location.model";
import { UserDTO } from "./registered-user"

export interface Post {
    id: number; // Jedinstveni identifikator posta
    description: string; // Opis objave
    imagePath: string; // Putanja do slike
    creationDateTime: Date;
    location: Location | null; // Lokacija objave
    usernameDisplay?: string;
    user?: UserDTO;
    likeCount: number;
    isLikedByCurrentUser?: boolean; 
    markedForAd?: boolean;
  }


