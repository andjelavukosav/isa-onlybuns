export interface UserDTO {
    id: number;
    firstname: string;
    lastname: string;
    email: string;
    followersCount: number;
    postsCount: number;
    username?: string;
    address: AddressDTO;
  }  

  export interface AddressDTO {
    country: String;
    city: String;
    street: String;
    streetNumber: String;
  }