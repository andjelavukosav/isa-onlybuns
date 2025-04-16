
export interface UserDTO {
    id: number;
    firstname: string;
    lastname: string;
    email: string;
    followersCount: number;
    followingCount: number;
    postsCount: number;
    username?: string;
  }  

export interface AuthUser{
  id: number;
  username: string;
  roles: string[];
}

export interface UserFollowStateDTO{
  followersCount: number;
  followingCount: number;
}

export interface UserSearchCriteria{
  firstName: string;
  lastName: string;
  email: string;
  minPostsCount: number | null;
  maxPostsCount: number | null;
}