import { Location } from "./location.model";
export interface Post {
    id: number; // Jedinstveni identifikator posta
    description: string; // Opis objave
    imagePath: string; // Putanja do slike
    creationDateTime: string; // Vreme kreiranja posta u ISO 8601 formatu
    location: Location; // Lokacija objave
  }
  
 
  