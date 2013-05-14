require File.expand_path("../../../config/environment", __FILE__)
require 'stalker'
include Stalker

RAILS_ENV = ENV["RAILS_ENV"] || "development"

job "uploader.create_fs_s3_photo_from_fs_photo" do |ids|
  photo = Photo.find_by_id(ids.first)
  

  unless photo.nil?
    save_time = Benchmark.realtime { photo.update_attribute( :s3_photo, photo.fs_photo) }
    photo.statistics.update_attribute( :fs_s3_photo_stats, save_time )
    FileUtils.remove_dir("#{Rails.root}/public/fs_uploads/photo/fs_photo/#{photo.id}", :force => true)
    photo.remove_fs_photo!
    photo.save
  end
end