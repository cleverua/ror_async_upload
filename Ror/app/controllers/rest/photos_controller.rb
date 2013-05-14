class Rest::PhotosController < ApplicationController

  UPLOAD_TYPE_FS = 'fs'
  UPLOAD_TYPE_S3 = 's3'

  def create
    @photo = Photo.new()

    if params[:photo].present? && params[:upload_type].present?
      if params[:upload_type] == UPLOAD_TYPE_FS
        @photo.fs_photo = params[:photo]
        @photo.size = params[:photo].size
        @photo.filename = params[:photo].original_filename
        if fs_photo_save(@photo)
          Stalker.enqueue('uploader.create_fs_s3_photo_from_fs_photo', [@photo.id])
          return_status_ok(@photo.id)
        else
          return_status_fail
        end
      else
        @photo.s3_photo = params[:photo]
        @photo.size = params[:photo].size
        @photo.filename = params[:photo].original_filename
        if s3_photo_save(@photo)
          return_status_ok(@photo.id)
        else
          return_status_fail
        end
      end
    else
      return_status_fail
    end
  end


  private


  def return_status_ok(id)
    respond_to do |format|
      format.json { render json: { status: "ok", id: id } }
    end
    return false
  end

  def return_status_fail
    respond_to do |format|
      format.json { render json: { status: "fail" } }
    end
    return false
  end

  def fs_photo_save(photo)
    save_time = Benchmark.realtime { photo.save }
    Statistics.create( photo: photo, fs_photo_stats: save_time )
  end

  def s3_photo_save(photo)
    save_time = Benchmark.realtime { photo.save }
    Statistics.create( photo: photo, s3_photo_stats: save_time )
  end

end