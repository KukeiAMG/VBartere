export interface Advertisement {
  id: number;
  title: string;
  description: string;
  imagesId: number[];
  subCategoryId: number;
  ownerId: number;
  status: boolean;
}

export interface AdvertisementDTO {
  title: string;
  description: string;
  subCategoryId: number;
  status: boolean;
  imagesId?: number[];
}
