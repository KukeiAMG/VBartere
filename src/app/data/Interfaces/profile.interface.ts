export interface Profile{
  id: number;
  phoneNumber: string;
  avatarUrl: string | null;
  subscribersAmount: number;
  name: string;
  surname: string | null;
  isActive: boolean;
  stack: string[];
  city: string | null;
  description: string | null;
}

