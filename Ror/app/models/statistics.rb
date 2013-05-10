class Statistics < ActiveRecord::Base
  attr_accessible :photo, :photo_id, :s3_photo_stats, :fs_photo_stats, :fs_s3_photo_stats

  belongs_to :photo
end
