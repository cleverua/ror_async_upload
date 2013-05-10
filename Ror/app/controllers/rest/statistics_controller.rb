class Rest::StatisticsController < ApplicationController

  def show
    @photo = Photo.find_by_id( params[:id] )

    unless @photo.nil?
      s3_time = ''
      fs_time = ''
      fs_s3_time = ''

      unless @photo.statistics.nil?
        s3_time = @photo.statistics.s3_photo_stats.to_s
        fs_time = @photo.statistics.fs_photo_stats.to_s
        fs_s3_time = @photo.statistics.fs_s3_photo_stats.to_s
      end

      if !s3_time.blank?
        respond_to do |format|
          format.json { render json: { status: "ok", type: Rest::PhotosController::UPLOAD_TYPE_S3, s3_time: s3_time } }
        end
      elsif !fs_time.blank? && !fs_s3_time.blank?
        respond_to do |format|
          format.json { render json: { status: "ok", type: Rest::PhotosController::UPLOAD_TYPE_FS, fs_time: fs_time, fs_s3_time: fs_s3_time } }
        end
      else
        respond_to do |format|
          format.json { render json: { status: "fail" } }
        end
      end

    else
      respond_to do |format|
        format.json { render json: { status: "fail" } }
      end
    end

  end

end