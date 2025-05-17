export interface Profile{
  id: number;
  phoneNumber: string;
  avatarUrl: string | null;
  email: string | null;
  name: string;
  surname: string | null;
  invitedByCode?: string | null;
  isActive: boolean;
  description: string | null;
}

