export interface UserDTO {
    id: number;
    firstname: string;
    lastname: string;
    email: string;
    followersCount: number;
    postsCount: number;
    username?: string;
  }  