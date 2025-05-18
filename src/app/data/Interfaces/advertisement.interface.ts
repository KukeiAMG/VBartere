export interface Advertisement {
  id: number;
  title: string;
  description: string | null;
  imageList: string[] | null;
  subCategoryId: number | null;
  ownerId: number | null;
  status: boolean | null;
}
export interface AdvertisementDTO {
  title: string;
  description: string;
  subCategoryId: number;
  ownerId: number;
  status: boolean;
  // Добавьте остальные необходимые поля
}
