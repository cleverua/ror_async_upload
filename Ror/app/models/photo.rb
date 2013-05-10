class Photo < ActiveRecord::Base
  attr_accessible :s3_photo, :fs_photo, :fs_s3_photo, :origin_size, :origin_height, :origin_width

  has_one :statistics

  MAIN_RETINA = 'main_retina'
  MAIN_MEDIUM = 'main_medium'
  THUMB_RETINA = 'thumb_retina'
  THUMB_MEDIUM = 'thumb_medium'

  mount_uploader :s3_photo, S3PhotoUploader
  mount_uploader :fs_photo, FsPhotoUploader
  mount_uploader :fs_s3_photo, S3PhotoUploader
end
