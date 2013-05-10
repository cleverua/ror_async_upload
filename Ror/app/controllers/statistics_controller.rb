class StatisticsController < ApplicationController

  def index
    @photos = Photo.page(params[:page] || 1).per(50)
  end

  def show
    @photo = Photo.find_by_id(params[:id])
  end

end
