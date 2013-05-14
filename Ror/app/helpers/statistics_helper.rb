module StatisticsHelper

  def photo_size(photo)
    if !photo.size.blank?
      "#{photo.size} kb"
    else
      "0 kb"
    end
  end

  def photo_filename(photo)
    if !photo.filename.blank?
      "Photo - #{photo.filename} - details"
    else
      "No Photo"
    end
  end

  def photo_original(photo)
    if !photo.s3_photo.path.blank?
      image_tag(photo.s3_photo.url, style: "width:700px !important")
    elsif !photo.fs_s3_photo.path.blank?
      image_tag(photo.fs_s3_photo.url, style: "width:700px !important")
    else
      image_tag("default.jpg", style: "width:80px !important")
    end
  end

  def photo_version(photo, version)
    width = 700
    case version
    when Photo::MAIN_RETINA
      width = 640
    when Photo::MAIN_MEDIUM
      width = 320
    when Photo::THUMB_RETINA
      width = 160
    when Photo::THUMB_MEDIUM
      width = 80
    end

    if !photo.s3_photo.path.blank?
      image_tag(photo.s3_photo.url(version), style: "width:#{width}px !important")
    elsif !photo.fs_s3_photo.path.blank?
      image_tag(photo.fs_s3_photo.url(version), style: "width:#{width}px !important")
    else
      image_tag("default.jpg", style: "width:80px !important")
    end
  end

end