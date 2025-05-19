export interface AdvertisementImage {
  id: number;
  size: number;
  name: string;
  originalFileName: string;
  contentType: string;
  filePath: string;
  previewImage: boolean;
}

export interface Advertisement {
  id: number;
  title: string;
  description: string;
  imageList: AdvertisementImage[];
  subCategoryId: number;
  ownerId: number;
  buyersId: number | null;
  status: boolean;
}

export interface AdvertisementDTO {
  title: string;
  description: string;
  subCategoryId: number;
  ownerId: number;
  status: boolean;
}
